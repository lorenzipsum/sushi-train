package com.lorenzipsum.sushitrain.backend.domain.belt;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItem;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class BeltSlotTest {

    private final MenuItem salmonNigiri = MenuItem.create("Salmon Nigiri", PlateTier.GREEN, new MoneyYen(450));
    private final Plate plate = Plate.create(salmonNigiri, PlateTier.GREEN, new MoneyYen(450), Instant.now().plusSeconds(7200));
    private final Plate plate2 = Plate.create(salmonNigiri, PlateTier.GREEN, new MoneyYen(450), Instant.now().plusSeconds(7200));

    @Test
    @DisplayName("Belt slot can be created with sane defaults")
    void emptyAt_creation_ok() {
        var belt = Belt.create("Default", 5);

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
                () -> assertEquals(4, lastSlot.getPositionIndex()),
                () -> assertSame(belt, lastSlot.getBelt()),
                () -> assertNull(lastSlot.getPlate()));

        belt.getSlots().forEach(slot -> assertSame(belt, slot.getBelt(), "each slot must reference its parent belt"));
    }

    @Test
    @DisplayName("All belt slots are empty in the beginning")
    void isEmpty_ok() {
        var belt = Belt.create("Default", 5);

        assertTrue(belt.getSlots().stream().allMatch(BeltSlot::isEmpty), "all slots should be empty initially");
    }

    @Test
    @DisplayName("Plate can be placed on belt slot")
    void place_ok() {
        // given
        var belt = Belt.create("Default", 5);

        // when
        var slot = belt.getSlots().getFirst();
        slot.place(plate);

        // then
        assertFalse(slot.isEmpty());
        assertSame(plate, slot.getPlate());
    }

    @Test
    @DisplayName("Only 1 plate can be placed on 1 belt slot")
    void place_only_one_plate_per_slot() {
        // given
        var belt = Belt.create("Default", 5);

        // when
        var slot = belt.getSlots().getFirst();
        slot.place(plate);

        // then
        assertThrows(IllegalStateException.class, () -> slot.place(plate2));
    }

    @Test
    @DisplayName("An empty plate (null) cannot be placed")
    void place_null_not_allowed() {
        var belt = Belt.create("Default", 5);
        var slot = belt.getSlots().getFirst();

        assertThrows(IllegalArgumentException.class, () -> slot.place(null));
    }

    @Test
    @DisplayName("place → take empties slot; can place again and take again; take() is idempotent")
    void place_take_place_again_ok() {
        var belt = Belt.create("Default", 5);
        var slot = belt.getSlots().getFirst();

        slot.place(plate);
        assertAll("Assert after first place/take",
                () -> assertSame(plate, slot.take()),
                () -> assertTrue(slot.isEmpty()));

        slot.place(plate2);
        assertAll("Assert after second place/take",
                () -> assertSame(plate2, slot.take()),
                () -> assertNull(slot.take()));
    }

    @Test
    @DisplayName("Plate can only be picked from belt slot if it exists")
    void takeStrict_happy_then_empty_throws() {
        var belt = Belt.create("Default", 5);
        var slot = belt.getSlots().getFirst();
        slot.place(plate);

        assertAll("Assert belt slot",
                () -> assertSame(plate, slot.takeStrict(), "takeStrict should return the exact placed plate"),
                () -> assertTrue(slot.isEmpty(), "slot must be empty after takeStrict"),
                () -> assertThrows(IllegalStateException.class, slot::takeStrict, "taking again from empty should throw"));
    }
}