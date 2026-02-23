package com.lorenzipsum.sushitrain.backend.application.belt;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltSlotEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class BeltSlotPlacementTest {

    @Test
    @DisplayName("pickSlots should pick first free slot then enforce min gap (when possible)")
    void pickSlots_enforces_gap() {
        // positions 0..19 are free
        var slots = slotsAtPositions(0, 20);

        var picked = BeltSlotPlacement.pickSlots(slots, 5, 3);

        // Expect 0, 5, 10
        assertThat(picked).extracting(BeltSlotEntity::getPositionIndex)
                .containsExactly(0, 5, 10);
    }

    @Test
    @DisplayName("pickSlots should top up ignoring gap when gap rule cannot reach requested count but enough free slots exist")
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

        var picked = BeltSlotPlacement.pickSlots(slots, 5, 3);

        // Pass 1 would pick [0, 5], then top-up adds next earliest not already picked -> [0, 5, 1]
        assertThat(picked).extracting(BeltSlotEntity::getPositionIndex)
                .containsExactly(0, 5, 1);
    }

    @Test
    @DisplayName("pickSlots should return empty when count is 0")
    void pickSlots_count_zero_returns_empty() {
        var slots = slotsAtPositions(0, 10);

        var picked = BeltSlotPlacement.pickSlots(slots, 5, 0);

        assertThat(picked).isEmpty();
    }

    @Test
    @DisplayName("pickSlots should return all free slots when requested count exceeds available")
    void pickSlots_count_exceeds_available_returns_all() {
        var slots = slotsAtPositions(0, 4); // 0,1,2,3

        var picked = BeltSlotPlacement.pickSlots(slots, 5, 10);

        assertThat(picked).extracting(BeltSlotEntity::getPositionIndex)
                .containsExactly(0, 1, 2, 3);
    }

    @SuppressWarnings("SameParameterValue")
    private static List<BeltSlotEntity> slotsAtPositions(int startInclusive, int endExclusive) {
        return IntStream.range(startInclusive, endExclusive)
                .mapToObj(BeltSlotPlacementTest::slot)
                .toList();
    }

    private static BeltSlotEntity slot(int positionIndex) {
        return new BeltSlotEntity(UUID.randomUUID(), positionIndex, null, null);
    }
}