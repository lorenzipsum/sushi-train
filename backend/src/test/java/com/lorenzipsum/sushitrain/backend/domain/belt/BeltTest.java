package com.lorenzipsum.sushitrain.backend.domain.belt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BeltTest {

    @Test
    @DisplayName("create(name, slotCount, seatSpecs) initializes belt with slots and sane defaults")
    void create_ok_initializeSlotsAndDefaults() {
        var belt = Belt.create("Main", 8, List.of());

        assertAll("Asserting sane defaults after creation",
                () -> assertNotNull(belt.getId(), "ID should be generated"),
                () -> assertEquals("Main", belt.getName(), "Belt name should be assigned correctly"),
                () -> assertEquals(8, belt.getSlotCount(), "Slot count should be assigned correctly"),
                () -> assertEquals(0, belt.getBaseRotationOffset(), "Rotation offset should start with 0"),
                () -> assertEquals(1000, belt.getTickIntervalMs(), "Default tick should be 1000ms"),
                () -> assertEquals(1, belt.getSpeedSlotsPerTick(), "Default speed should be 1 slot per tick"),
                () -> assertNotNull(belt.getSlots(), "Slots should be initialized"),
                () -> assertEquals(8, belt.getSlots().size(), "One slot per index 0, 1, ... 7"),
                () -> assertNotNull(belt.getSeats(), "Seats list should be initialized"),
                () -> assertTrue(belt.getSeats().isEmpty(), "No seats expected when seatSpecs is empty")
        );

        var indices = new HashSet<Integer>();
        belt.getSlots().forEach(item -> indices.add(item.getPositionIndex()));
        assertEquals(8, indices.size(), "Each slot index from 0 to 7 must exist exactly once");
        for (int i = 0; i < 8; i++) {
            assertTrue(indices.contains(i), "Slot index " + i + " should exist");
        }
    }

    @Test
    @DisplayName("create checks illegal values for belt creation")
    void create_not_ok() {
        assertAll("Asserting handling of unsuccessful creation",
                () -> assertThrows(IllegalArgumentException.class, () -> Belt.create("Default", 0, List.of())),
                () -> assertThrows(IllegalArgumentException.class, () -> Belt.create("Default", -1, List.of())),
                () -> assertThrows(IllegalArgumentException.class, () -> Belt.create(null, 1, List.of())),
                () -> assertThrows(IllegalArgumentException.class, () -> Belt.create("", 1, List.of())),
                () -> assertThrows(IllegalArgumentException.class, () -> Belt.create(" ", 1, List.of()))
        );
    }

    @Test
    @DisplayName("offset wraps correctly with default settings")
    void offset_with_defaults_wraps() {
        var belt = Belt.create("Wrap", 4, List.of());
        var offset = belt.getOffsetStartedAt();

        assertEquals(0, belt.currentOffsetAt(offset.plusMillis(999L)));
        assertEquals(1, belt.currentOffsetAt(offset.plusMillis(1000L)));
        assertEquals(2, belt.currentOffsetAt(offset.plusMillis(2000L)));
        assertEquals(3, belt.currentOffsetAt(offset.plusMillis(3000L)));
        assertEquals(3, belt.currentOffsetAt(offset.plusMillis(3999L)));
        assertEquals(0, belt.currentOffsetAt(offset.plusMillis(4000L)));
        assertEquals(0, belt.currentOffsetAt(offset.plusMillis(4999L)));
        assertEquals(1, belt.currentOffsetAt(offset.plusMillis(5000L)));
        assertEquals(2, belt.currentOffsetAt(offset.plusMillis(6000L)));
        assertEquals(3, belt.currentOffsetAt(offset.plusMillis(7000L)));
        assertEquals(0, belt.currentOffsetAt(offset.plusMillis(8000L)));
    }

    @Test
    @DisplayName("speedSlotsPerTick can be updated")
    void setSpeedSlotsPerTick_updates() {
        var belt = Belt.create("Default", 10, List.of());
        var now = Instant.now();

        assertEquals(1, belt.getSpeedSlotsPerTick());

        belt.setSpeedSlotsPerTick(5, now);
        assertEquals(5, belt.getSpeedSlotsPerTick());

        belt.setSpeedSlotsPerTick(2, now);
        assertEquals(2, belt.getSpeedSlotsPerTick());

        belt.setSpeedSlotsPerTick(1, now);
        assertEquals(1, belt.getSpeedSlotsPerTick());

        belt.setSpeedSlotsPerTick(0, now);
        assertEquals(1, belt.getSpeedSlotsPerTick());

        belt.setSpeedSlotsPerTick(-1, now);
        assertEquals(1, belt.getSpeedSlotsPerTick());

        assertThrows(IllegalArgumentException.class, () -> belt.setSpeedSlotsPerTick(10, now));
    }

    @Test
    @DisplayName("offset wraps correctly with higher number of slots per tick")
    void offset_with_custom_speed_wraps() {
        var belt = Belt.create("Speedy", 10, List.of());
        var now = Instant.now();

        belt.setSpeedSlotsPerTick(3, now);

        assertEquals(0, belt.currentOffsetAt(now.plusMillis(999L)));
        assertEquals(3, belt.currentOffsetAt(now.plusMillis(1000L)));
        assertEquals(6, belt.currentOffsetAt(now.plusMillis(2000L)));
        assertEquals(9, belt.currentOffsetAt(now.plusMillis(3000L)));
        assertEquals(2, belt.currentOffsetAt(now.plusMillis(4000L)));
        assertEquals(5, belt.currentOffsetAt(now.plusMillis(5000L)));
    }

    @Test
    @DisplayName("tickIntervalMs can be updated")
    void setTickInterval_updates() {
        var belt = Belt.create("Slowy", 10, List.of());
        var now = Instant.now();

        assertEquals(1000, belt.getTickIntervalMs());

        belt.setTickIntervalMs(2000, now);
        assertEquals(2000, belt.getTickIntervalMs());

        belt.setTickIntervalMs(2, now);
        assertEquals(2, belt.getTickIntervalMs());

        belt.setTickIntervalMs(1, now);
        assertEquals(1, belt.getTickIntervalMs());

        belt.setTickIntervalMs(0, now);
        assertEquals(1, belt.getTickIntervalMs());

        belt.setTickIntervalMs(-1, now);
        assertEquals(1, belt.getTickIntervalMs());
    }

    @Test
    @DisplayName("offset wraps correctly with higher tick rate")
    void offset_with_custom_tick_interval_wraps() {
        var belt = Belt.create("Speedy", 4, List.of());
        var now = Instant.now();

        belt.setTickIntervalMs(250, now);

        assertEquals(0, belt.currentOffsetAt(now.plusMillis(249)));
        assertEquals(1, belt.currentOffsetAt(now.plusMillis(250L)));
        assertEquals(2, belt.currentOffsetAt(now.plusMillis(500L)));
        assertEquals(3, belt.currentOffsetAt(now.plusMillis(750L)));
        assertEquals(3, belt.currentOffsetAt(now.plusMillis(999L)));
        assertEquals(0, belt.currentOffsetAt(now.plusMillis(1000L)));
        assertEquals(1, belt.currentOffsetAt(now.plusMillis(1250L)));
        assertEquals(2, belt.currentOffsetAt(now.plusMillis(1500L)));
    }

    @Test
    @DisplayName("getSlots and getSeats return unmodifiable views")
    void collections_are_immutable() {
        var belt = Belt.create("Immutable", 10, List.of(new SeatSpec("A", 0)));

        var slots = belt.getSlots();
        var seats = belt.getSeats();

        assertThrows(UnsupportedOperationException.class, slots::clear);
        assertThrows(UnsupportedOperationException.class, seats::clear);
    }
}
