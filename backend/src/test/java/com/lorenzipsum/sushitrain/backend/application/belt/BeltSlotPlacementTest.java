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
    @DisplayName("pickSlots should pick first free slot then enforce min gap")
    void pickSlots_enforces_gap() {
        // positions 0..19 are free
        var slots = slotsAtPositions(0, 20);

        var picked = BeltSlotPlacement.pickSlots(slots, 5, 3);

        // Expect 0, 5, 10
        assertThat(picked).extracting(BeltSlotEntity::getPositionIndex)
                .containsExactly(0, 5, 10);
    }

    @Test
    @DisplayName("pickSlots should return fewer slots when gap rule prevents reaching requested count")
    void pickSlots_returns_fewer_when_gap_prevents_count() {
        // Free slots only at 0,1,2,3,4,5 (dense)
        var slots = List.of(
                slot(0),
                slot(1),
                slot(2),
                slot(3),
                slot(4),
                slot(5)
        );

        var picked = BeltSlotPlacement.pickSlots(slots, 5, 3);

        // With min gap 5 and no further positions, only 0 and 5 are valid.
        assertThat(picked).extracting(BeltSlotEntity::getPositionIndex)
                .containsExactly(0, 5);
    }

    @Test
    @DisplayName("pickSlots should return empty when count is 0")
    void pickSlots_count_zero_returns_empty() {
        var slots = slotsAtPositions(0, 10);

        var picked = BeltSlotPlacement.pickSlots(slots, 5, 0);

        assertThat(picked).isEmpty();
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