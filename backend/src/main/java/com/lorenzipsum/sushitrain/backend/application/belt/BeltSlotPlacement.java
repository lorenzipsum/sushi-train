package com.lorenzipsum.sushitrain.backend.application.belt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

final class BeltSlotPlacement {
    private BeltSlotPlacement() {
    }

    /**
     * Picks slots from a list already ordered by positionIndex.
     * Rules:
     * 1) Prefer gap-respecting placement: first chosen slot is the first free one; next must be at least minGapSlots ahead.
     * Wrap-around ignored by design.
     * 2) If pass (1) cannot reach requested count, but there are enough free slots overall, then "top up"
     * by adding more free slots in order, ignoring the gap rule, without removing already picked ones.
     */
    static List<BeltSlotAllocationCommandPort.FreeBeltSlot> pickSlots(List<BeltSlotAllocationCommandPort.FreeBeltSlot> freeSlotsOrdered, int minGapSlots, int count) {
        if (count <= 0 || freeSlotsOrdered == null || freeSlotsOrdered.isEmpty()) {
            return List.of();
        }

        int target = Math.min(count, freeSlotsOrdered.size());

        if (minGapSlots <= 0) {
            return new ArrayList<>(freeSlotsOrdered.subList(0, target));
        }

        // Pass 1: gap-respecting
        var picked = new ArrayList<BeltSlotAllocationCommandPort.FreeBeltSlot>(target);
        Integer lastPos = null;

        for (var slot : freeSlotsOrdered) {
            if (picked.size() >= target) break;

            int pos = slot.positionIndex();
            if (lastPos == null || (pos - lastPos) >= minGapSlots) {
                picked.add(slot);
                lastPos = pos;
            }
        }

        // Pass 2: top-up ignoring gaps (only if there are enough free slots overall to satisfy target)
        if (picked.size() < target) {
            var pickedIds = new HashSet<>(picked.size());
            for (var s : picked) {
                pickedIds.add(s.slotId());
            }

            for (var slot : freeSlotsOrdered) {
                if (picked.size() >= target) break;

                if (pickedIds.add(slot.slotId())) {
                    picked.add(slot);
                }
            }
        }

        return picked;
    }
}
