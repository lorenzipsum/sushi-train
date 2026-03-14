package com.lorenzipsum.sushitrain.backend.application.belt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class BeltSlotPlacementTest {

    @Test
    @DisplayName("pickSlots should spread placements around the circular belt when possible")
    void pickSlots_enforces_gap() {
        // positions 0..19 are free
        var slots = slotsAtPositions(0, 20);

        var picked = BeltSlotPlacement.pickSlots(slots, 20, 5, 3);

        // Circular farthest-point placement on an empty belt yields an even spread.
        assertThat(picked).extracting(BeltSlotAllocationCommandPort.FreeBeltSlot::positionIndex)
                .containsExactly(0, 5, 10);
    }

    @Test
    @DisplayName("pickSlots should respect wrap-around and avoid treating belt edges as far apart")
    void pickSlots_tops_up_when_gap_prevents_count() {
        // Dense free slots: 0..5
        var slots = List.of(
                slot(0),
                slot(1),
                slot(2),
                slot(3),
                slot(4),
                slot(5)
        );

        var picked = BeltSlotPlacement.pickSlots(slots, 6, 5, 3);

        // Only one slot can satisfy the gap on a 6-slot circle with a min gap of 5, then balanced fallback tops up.
        assertThat(picked).extracting(BeltSlotAllocationCommandPort.FreeBeltSlot::positionIndex)
                .containsExactly(0, 1, 3);
    }

    @Test
    @DisplayName("pickSlots should return empty when count is 0")
    void pickSlots_count_zero_returns_empty() {
        var slots = slotsAtPositions(0, 10);

        var picked = BeltSlotPlacement.pickSlots(slots, 10, 5, 0);

        assertThat(picked).isEmpty();
    }

    @Test
    @DisplayName("pickSlots should return all free slots when requested count exceeds available")
    void pickSlots_count_exceeds_available_returns_all() {
        var slots = slotsAtPositions(0, 4); // 0,1,2,3

        var picked = BeltSlotPlacement.pickSlots(slots, 4, 5, 10);

        assertThat(picked).extracting(BeltSlotAllocationCommandPort.FreeBeltSlot::positionIndex)
                .containsExactly(0, 1, 2, 3);
    }

    @Test
    @DisplayName("pickSlots should fill the largest circular gaps between existing occupied slots")
    void pickSlots_prefers_largest_gaps_between_existing_plates() {
        var slots = List.of(
                slot(2),
                slot(3),
                slot(4),
                slot(7),
                slot(8),
                slot(9)
        );

        var picked = BeltSlotPlacement.pickSlots(slots, 12, 3, 2);

        assertThat(picked).extracting(BeltSlotAllocationCommandPort.FreeBeltSlot::positionIndex)
                .containsExactly(3, 8);
    }

    @SuppressWarnings("SameParameterValue")
    private static List<BeltSlotAllocationCommandPort.FreeBeltSlot> slotsAtPositions(int startInclusive, int endExclusive) {
        return IntStream.range(startInclusive, endExclusive)
                .mapToObj(BeltSlotPlacementTest::slot)
                .toList();
    }

    private static BeltSlotAllocationCommandPort.FreeBeltSlot slot(int positionIndex) {
        return new BeltSlotAllocationCommandPort.FreeBeltSlot(UUID.randomUUID(), positionIndex);
    }
}
