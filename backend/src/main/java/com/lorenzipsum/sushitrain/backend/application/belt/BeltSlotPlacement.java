package com.lorenzipsum.sushitrain.backend.application.belt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

final class BeltSlotPlacement {
    private BeltSlotPlacement() {
    }

    /**
     * Picks slots from a list already ordered by positionIndex.
     * Rules:
     * 1) Prefer balanced circular placement: repeatedly pick the free slot farthest away from occupied/newly picked slots.
     * 2) Enforce the minimum circular distance between newly picked slots when possible.
     * 3) If pass (1) cannot reach the requested count, top up with the same balanced strategy without the gap constraint.
     */
    static List<BeltSlotAllocationCommandPort.FreeBeltSlot> pickSlots(List<BeltSlotAllocationCommandPort.FreeBeltSlot> freeSlotsOrdered,
                                                                      int slotCount,
                                                                      int minGapSlots,
                                                                      int count) {
        if (count <= 0 || freeSlotsOrdered == null || freeSlotsOrdered.isEmpty()) {
            return List.of();
        }
        if (slotCount <= 0) {
            throw new IllegalArgumentException("slotCount must be > 0");
        }

        int target = Math.min(count, freeSlotsOrdered.size());
        int maxPosition = freeSlotsOrdered.getLast().positionIndex();
        if (maxPosition >= slotCount) {
            throw new IllegalArgumentException("slotCount must be greater than every free slot position");
        }

        var remaining = new ArrayList<>(freeSlotsOrdered);
        var freePositions = remaining.stream()
                .map(BeltSlotAllocationCommandPort.FreeBeltSlot::positionIndex)
                .collect(Collectors.toSet());
        var occupiedPositions = IntStream.range(0, slotCount)
                .filter(position -> !freePositions.contains(position))
                .boxed()
                .collect(Collectors.toSet());
        var picked = new ArrayList<BeltSlotAllocationCommandPort.FreeBeltSlot>(target);
        var pickedPositions = new ArrayList<Integer>(target);

        while (picked.size() < target) {
            var candidate = pickBestCandidate(remaining, occupiedPositions, pickedPositions, slotCount, minGapSlots, true);
            if (candidate == null) {
                break;
            }
            picked.add(candidate);
            pickedPositions.add(candidate.positionIndex());
            remaining.remove(candidate);
        }

        while (picked.size() < target) {
            var candidate = pickBestCandidate(remaining, occupiedPositions, pickedPositions, slotCount, minGapSlots, false);
            if (candidate == null) {
                break;
            }
            picked.add(candidate);
            pickedPositions.add(candidate.positionIndex());
            remaining.remove(candidate);
        }

        picked.sort(Comparator.comparingInt(BeltSlotAllocationCommandPort.FreeBeltSlot::positionIndex));
        return picked;
    }

    private static BeltSlotAllocationCommandPort.FreeBeltSlot pickBestCandidate(List<BeltSlotAllocationCommandPort.FreeBeltSlot> candidates,
                                                                                 Set<Integer> occupiedPositions,
                                                                                 List<Integer> pickedPositions,
                                                                                 int slotCount,
                                                                                 int minGapSlots,
                                                                                 boolean enforceGap) {
        BeltSlotAllocationCommandPort.FreeBeltSlot best = null;
        int bestScore = Integer.MIN_VALUE;

        for (var candidate : candidates) {
            int candidatePosition = candidate.positionIndex();
            if (enforceGap && !respectsMinGap(candidatePosition, pickedPositions, slotCount, minGapSlots)) {
                continue;
            }

            int score = scoreCandidate(candidatePosition, occupiedPositions, pickedPositions, slotCount);
            if (best == null || score > bestScore || (score == bestScore && candidatePosition < best.positionIndex())) {
                best = candidate;
                bestScore = score;
            }
        }

        return best;
    }

    private static boolean respectsMinGap(int candidatePosition, List<Integer> pickedPositions, int slotCount, int minGapSlots) {
        if (minGapSlots <= 0) {
            return true;
        }
        for (int pickedPosition : pickedPositions) {
            if (circularDistance(candidatePosition, pickedPosition, slotCount) < minGapSlots) {
                return false;
            }
        }
        return true;
    }

    private static int scoreCandidate(int candidatePosition, Set<Integer> occupiedPositions, List<Integer> pickedPositions, int slotCount) {
        if (occupiedPositions.isEmpty() && pickedPositions.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        int bestDistance = Integer.MAX_VALUE;
        for (int occupiedPosition : occupiedPositions) {
            bestDistance = Math.min(bestDistance, circularDistance(candidatePosition, occupiedPosition, slotCount));
        }
        for (int pickedPosition : pickedPositions) {
            bestDistance = Math.min(bestDistance, circularDistance(candidatePosition, pickedPosition, slotCount));
        }
        return bestDistance;
    }

    private static int circularDistance(int left, int right, int slotCount) {
        int directDistance = Math.abs(left - right);
        return Math.min(directDistance, slotCount - directDistance);
    }
}
