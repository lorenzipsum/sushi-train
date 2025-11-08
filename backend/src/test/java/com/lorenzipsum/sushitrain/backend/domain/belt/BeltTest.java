package com.lorenzipsum.sushitrain.backend.domain.belt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BeltTest {


    @Test
    @DisplayName("create(name, slotCount) initializes belt with slots and sane defaults")
    void create_initializeSlotsAndDefaults() {
        Belt belt = Belt.create("Main", 8);

        assertNotNull(belt.getId(), "ID should be generated");
        assertEquals("Main", belt.getName(), "Belt name should be assigned correctly");
        assertEquals(8, belt.getSlotCount(), "Slot count should be assigend correctly");
        assertEquals(0, belt.getRotationOffset(), "Rotation offset should start with 0");
        assertEquals(1000, belt.getTickIntervalMs(), "Default tick should be 1000ms");
        assertEquals(1, belt.getSpeedSlotsPerTick(), "Default speed should be 1 slot per tick");

        assertNotNull(belt.getSlots(), "Slots should be initialized");
        assertEquals(8, belt.getSlots().size(), "One slot per index 0, 1, ... 7");

        Set<Integer> indices = new HashSet<>();
        belt.getSlots().forEach(item -> indices.add(item.getPositionIndex()));
        assertEquals(8, indices.size(), "Each slot index from 0 to 7 must exist exactly once");
        for (int i = 0; i < 8; i++) {
            assertTrue(indices.contains(i), "Slot index " + i + " should exist");
        }
    }

    @Test
    @DisplayName("advanceOffset increases offset and wraps around modulo slotCount")
    void advanceOffset_wraps() {
        Belt belt = Belt.create("Wrap", 4);
        assertEquals(0, belt.getRotationOffset());

        belt.advanceOffset();
        assertEquals(1, belt.getRotationOffset());
        belt.advanceOffset();
        assertEquals(2, belt.getRotationOffset());
        belt.advanceOffset();
        assertEquals(3, belt.getRotationOffset());

        belt.advanceOffset();
        assertEquals(0, belt.getRotationOffset());
        belt.advanceOffset();
        assertEquals(1, belt.getRotationOffset());
    }

    @Test
    @DisplayName("advanceOffset respects speedSlotsPerTick")
    void advanceOffset_customSpeedWraps() {
        // given
        Belt belt = Belt.create("Speedy", 10);

        // when
        belt.setSpeedSlotsPerTick(3);

        // then
        belt.advanceOffset();
        assertEquals(3, belt.getRotationOffset());
        belt.advanceOffset();
        assertEquals(6, belt.getRotationOffset());
        belt.advanceOffset();
        assertEquals(9, belt.getRotationOffset());

        belt.advanceOffset();
        assertEquals(2, belt.getRotationOffset());
        belt.advanceOffset();
        assertEquals(5, belt.getRotationOffset());
    }

    @Test
    @DisplayName("setSpeedSlotsPerTick enforces minimum of 1")
    void setSpeed_minimumOne() {
        Belt belt = Belt.create("Speed", 10);

        belt.setSpeedSlotsPerTick(0);
        assertEquals(1, belt.getSpeedSlotsPerTick());

        belt.setSpeedSlotsPerTick(-5);
        assertEquals(1, belt.getSpeedSlotsPerTick());

        belt.setSpeedSlotsPerTick(2);
        assertEquals(2, belt.getSpeedSlotsPerTick());
    }

    @Test
    @DisplayName("tickIntervalMs can be updated")
    void setTickInterval_updates() {
        Belt belt = Belt.create("Slowy", 10);

        belt.setTickIntervalMs(2000);
        assertEquals(2000, belt.getTickIntervalMs());
    }

    @Test
    @DisplayName("getSlots returns an unmodifiable view")
    void slots_areUnmodifiable() {
        Belt belt = Belt.create("Immutable", 10);

        var slots = belt.getSlots();

        assertThrows(UnsupportedOperationException.class, slots::clear);
        assertThrows(UnsupportedOperationException.class, slots::removeFirst);
    }

    @Test
    @DisplayName("slotCount is clamped to at least 1")
    void slotCount_minimumOne() {
        Belt belt = Belt.create("ZeroTest", 0);
        assertEquals(1, belt.getSlotCount());

        Belt belt2 = Belt.create("BelowZeroTest", -5);
        assertEquals(1, belt.getSlotCount());
    }
}