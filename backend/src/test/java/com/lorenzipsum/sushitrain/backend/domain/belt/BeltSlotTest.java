package com.lorenzipsum.sushitrain.backend.domain.belt;

import com.lorenzipsum.sushitrain.backend.domain.TestData;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeltSlotTest {

    private final Plate plate1 = TestData.plateSalmonNigiri();
    private final Plate plate2 = TestData.plateSalmonNigiri();

    @Test
    @DisplayName("Belt slot can be created with sane defaults")
    void emptyAt_creation_ok() {
        var belt = TestData.defaultBelt();

        var firstSlot = belt.getSlots().getFirst();
        var nextSlot = belt.getSlots().get(1);
        var lastSlot = belt.getSlots().getLast();

        assertAll("Assert first belt slot",
                () -> assertNotNull(firstSlot.getId()),
                () -> assertEquals(0, firstSlot.getPositionIndex()),
                () -> assertSame(belt, firstSlot.getBelt()),
                () -> assertNull(firstSlot.getPlate()));

        assertAll("Assert belt slot after first slot",
                () -> assertNotNull(nextSlot.getId()),
                () -> assertEquals(1, nextSlot.getPositionIndex()),
                () -> assertSame(belt, nextSlot.getBelt()),
                () -> assertNull(nextSlot.getPlate()));

        assertAll("Assert last belt slot",
                () -> assertNotNull(lastSlot.getId()),
                () -> assertEquals(9, lastSlot.getPositionIndex()),
                () -> assertSame(belt, lastSlot.getBelt()),
                () -> assertNull(lastSlot.getPlate()));

        belt.getSlots().forEach(slot -> assertSame(belt, slot.getBelt(), "each slot must reference its parent belt"));
    }

    @Test
    @DisplayName("All belt slots are empty in the beginning")
    void isEmpty_ok() {
        var belt = TestData.defaultBelt();

        assertTrue(belt.getSlots().stream().allMatch(BeltSlot::isEmpty), "all slots should be empty initially");
    }

    @Test
    @DisplayName("Plate can be placed on belt slot")
    void place_ok() {
        // given
        var belt = TestData.defaultBelt();

        // when
        var slot = belt.getSlots().getFirst();
        slot.place(plate1);

        // then
        assertFalse(slot.isEmpty());
        assertSame(plate1, slot.getPlate());
    }

    @Test
    @DisplayName("Only 1 plate can be placed on 1 belt slot")
    void place_only_one_plate_per_slot() {
        // given
        var belt = TestData.defaultBelt();

        // when
        var slot = belt.getSlots().getFirst();
        slot.place(plate1);

        // then
        assertThrows(IllegalStateException.class, () -> slot.place(plate2));
    }

    @Test
    @DisplayName("An empty plate (null) cannot be placed")
    void place_null_not_allowed() {
        var belt = TestData.defaultBelt();
        var slot = belt.getSlots().getFirst();

        assertThrows(IllegalArgumentException.class, () -> slot.place(null));
    }

    @Test
    @DisplayName("place → take empties slot; can place again and take again; take() is idempotent")
    void place_take_place_again_ok() {
        var belt = TestData.defaultBelt();
        var slot = belt.getSlots().getFirst();

        slot.place(plate1);
        assertAll("Assert after first place/take",
                () -> assertSame(plate1, slot.take()),
                () -> assertTrue(slot.isEmpty()));

        slot.place(plate2);
        assertAll("Assert after second place/take",
                () -> assertSame(plate2, slot.take()),
                () -> assertNull(slot.take()));
    }

    @Test
    @DisplayName("Plate can only be picked from belt slot if it exists")
    void takeStrict_happy_then_empty_throws() {
        var belt = TestData.defaultBelt();
        var slot = belt.getSlots().getFirst();
        slot.place(plate1);

        assertAll("Assert belt slot",
                () -> assertSame(plate1, slot.takeStrict(), "takeStrict should return the exact placed plate"),
                () -> assertTrue(slot.isEmpty(), "slot must be empty after takeStrict"),
                () -> assertThrows(IllegalStateException.class, slot::takeStrict, "taking again from empty should throw"));
    }
}