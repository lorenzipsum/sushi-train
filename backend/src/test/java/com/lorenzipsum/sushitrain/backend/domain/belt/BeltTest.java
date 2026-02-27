package com.lorenzipsum.sushitrain.backend.domain.belt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

import static com.lorenzipsum.sushitrain.backend.domain.belt.Belt.*;
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
                () -> assertEquals(TICK_INTERVAL_MS_DEFAULT_VALUE, belt.getTickIntervalMs(), "Default tick should be 1000ms"),
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
                () -> assertThrows(IllegalArgumentException.class, () -> Belt.create("Test Belt", 0, List.of())),
                () -> assertThrows(IllegalArgumentException.class, () -> Belt.create("Test Belt", -1, List.of())),
                () -> assertThrows(IllegalArgumentException.class, () -> Belt.create(null, 1, List.of())),
                () -> assertThrows(IllegalArgumentException.class, () -> Belt.create("", 1, List.of())),
                () -> assertThrows(IllegalArgumentException.class, () -> Belt.create(" ", 1, List.of()))
        );
    }

    @Test
    @DisplayName("offset wraps correctly with default settings")
    void offset_with_defaults_wraps() {
        var belt = Belt.create("Wrap", 4, List.of());
        belt.setTickIntervalMs(1000, Instant.now());
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
    void setSpeedSlotsPerTick_limits_ok() {
        var beltA = Belt.create("Test Belt", 10, List.of());
        var now = Instant.now();

        assertEquals(SPEED_SLOTS_PER_TICK_DEFAULT_VALUE, beltA.getSpeedSlotsPerTick());

        beltA.setSpeedSlotsPerTick(2, now);
        assertEquals(2, beltA.getSpeedSlotsPerTick());

        beltA.setSpeedSlotsPerTick(SPEED_SLOTS_PER_TICK_MAX_VALUE, now);
        assertEquals(SPEED_SLOTS_PER_TICK_MAX_VALUE, beltA.getSpeedSlotsPerTick());

        beltA.setSpeedSlotsPerTick(SPEED_SLOTS_PER_TICK_MAX_VALUE + 1, now);
        assertEquals(SPEED_SLOTS_PER_TICK_MAX_VALUE, beltA.getSpeedSlotsPerTick());

        beltA.setSpeedSlotsPerTick(SPEED_SLOTS_PER_TICK_MIN_VALUE, now);
        assertEquals(SPEED_SLOTS_PER_TICK_MIN_VALUE, beltA.getSpeedSlotsPerTick());

        beltA.setSpeedSlotsPerTick(SPEED_SLOTS_PER_TICK_MIN_VALUE - 1, now);
        assertEquals(SPEED_SLOTS_PER_TICK_MIN_VALUE, beltA.getSpeedSlotsPerTick());
    }

    @Test
    @DisplayName("setSpeedSlotsPerTick with speed higher than slot count is capped")
    void setSpeedSlotsPerTick_with_speed_higher_than_slot_count_is_capped() {
        int slotCount = 4;
        var belt = Belt.create("Test Belt", slotCount, List.of());
        belt.setSpeedSlotsPerTick(slotCount, Instant.now());
        assertEquals(slotCount - 1, belt.getSpeedSlotsPerTick(), "Speed should be capped to slotCount - 1");
    }

    @Test
    @DisplayName("setSpeedSlotsPerTick with null timestamp throws")
    void setSpeedSlotsPerTick_with_null_timestamp_throws() {
        var belt = Belt.create("Test Belt", 10, List.of());
        assertThrows(IllegalArgumentException.class, () -> belt.setSpeedSlotsPerTick(2, null));
    }

    @Test
    @DisplayName("offset wraps correctly with higher number of slots per tick")
    void offset_with_custom_speed_wraps() {
        var belt = Belt.create("Speedy", 10, List.of());
        var now = Instant.now();
        belt.setTickIntervalMs(1000, now);
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
    void setTickInterval_limits_ok() {
        var belt = Belt.create("Slowy", 10, List.of());
        var now = Instant.now();

        assertEquals(TICK_INTERVAL_MS_DEFAULT_VALUE, belt.getTickIntervalMs());

        belt.setTickIntervalMs(TICK_INTERVAL_MS_MIN_VALUE, now);
        assertEquals(TICK_INTERVAL_MS_MIN_VALUE, belt.getTickIntervalMs());

        belt.setTickIntervalMs(TICK_INTERVAL_MS_MIN_VALUE - 1, now);
        assertEquals(TICK_INTERVAL_MS_MIN_VALUE, belt.getTickIntervalMs());

        belt.setTickIntervalMs(TICK_INTERVAL_MS_MAX_VALUE, now);
        assertEquals(TICK_INTERVAL_MS_MAX_VALUE, belt.getTickIntervalMs());

        belt.setTickIntervalMs(TICK_INTERVAL_MS_MAX_VALUE + 1, now);
        assertEquals(TICK_INTERVAL_MS_MAX_VALUE, belt.getTickIntervalMs());
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
