package com.lorenzipsum.sushitrain.backend.domain.belt;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.lorenzipsum.sushitrain.backend.testutil.TestFixtures.inTwoHours;
import static org.junit.jupiter.api.Assertions.*;

class BeltSlotTest {

    private final Plate plate1 = Plate.create(UUID.randomUUID(), PlateTier.GREEN, new MoneyYen(450), inTwoHours());
    private final Plate plate2 = Plate.create(UUID.randomUUID(), PlateTier.GREEN, new MoneyYen(450), inTwoHours());

    @Test
    @DisplayName("Belt creation creates slots with sane defaults")
    void slots_created_with_sane_defaults() {
        var belt = Belt.create("Default", 10, List.of());

        var firstSlot = belt.getSlots().getFirst();
        var nextSlot = belt.getSlots().get(1);
        var lastSlot = belt.getSlots().getLast();

        assertAll("Assert first belt slot",
                () -> assertNotNull(firstSlot.getId()),
                () -> assertEquals(0, firstSlot.getPositionIndex()),
                () -> assertNull(firstSlot.getPlateId()));

        assertAll("Assert belt slot after first slot",
                () -> assertNotNull(nextSlot.getId()),
                () -> assertEquals(1, nextSlot.getPositionIndex()),
                () -> assertNull(nextSlot.getPlateId()));

        assertAll("Assert last belt slot",
                () -> assertNotNull(lastSlot.getId()),
                () -> assertEquals(9, lastSlot.getPositionIndex()),
                () -> assertNull(lastSlot.getPlateId()));
    }

    @Test
    @DisplayName("All belt slots are empty in the beginning")
    void isEmpty_ok() {
        var belt = Belt.create("Default", 10, List.of());

        assertTrue(belt.getSlots().stream().allMatch(BeltSlot::isEmpty), "all slots should be empty initially");
    }

    @Test
    @DisplayName("Plate can be placed on belt slot")
    void place_ok() {
        var belt = Belt.create("Default", 10, List.of());

        var slot = belt.getSlots().getFirst();
        slot.place(plate1.getId());

        assertFalse(slot.isEmpty());
        assertEquals(plate1.getId(), slot.getPlateId());
    }

    @Test
    @DisplayName("Only 1 plate can be placed on 1 belt slot")
    void place_only_one_plate_per_slot() {
        var belt = Belt.create("Default", 10, List.of());

        var slot = belt.getSlots().getFirst();
        slot.place(plate1.getId());

        assertThrows(IllegalStateException.class, () -> slot.place(plate2.getId()));
    }

    @Test
    @DisplayName("An empty plate (null) cannot be placed")
    void place_null_not_allowed() {
        var belt = Belt.create("Default", 10, List.of());
        var slot = belt.getSlots().getFirst();

        assertThrows(IllegalArgumentException.class, () -> slot.place(null));
    }

    @Test
    @DisplayName("place → take empties slot; can place again and take again; take() is idempotent")
    void place_take_place_again_ok() {
        var belt = Belt.create("Default", 10, List.of());
        var slot = belt.getSlots().getFirst();

        slot.place(plate1.getId());
        assertAll("Assert after first place/take",
                () -> assertEquals(plate1.getId(), slot.take()),
                () -> assertTrue(slot.isEmpty()));

        slot.place(plate2.getId());
        assertAll("Assert after second place/take",
                () -> assertEquals(plate2.getId(), slot.take()),
                () -> assertNull(slot.take()));
    }

    @Test
    @DisplayName("Plate can only be picked from belt slot if it exists")
    void takeStrict_happy_then_empty_throws() {
        var belt = Belt.create("Default", 10, List.of());
        var slot = belt.getSlots().getFirst();
        slot.place(plate1.getId());

        assertAll("Assert belt slot",
                () -> assertEquals(plate1.getId(), slot.takeStrict(), "takeStrict should return the exact placed plate"),
                () -> assertTrue(slot.isEmpty(), "slot must be empty after takeStrict"),
                () -> assertThrows(IllegalStateException.class, slot::takeStrict, "taking again from empty should throw"));
    }
}
