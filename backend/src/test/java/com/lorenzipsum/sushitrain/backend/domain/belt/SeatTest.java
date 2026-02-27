package com.lorenzipsum.sushitrain.backend.domain.belt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SeatTest {

    @Test
    @DisplayName("Seats are created from SeatSpecs and are accessible from the belt")
    void create_seats_ok() {
        var seatSpecs = List.of(
                new SeatSpec("A", 0),
                new SeatSpec("B", 5),
                new SeatSpec("C", 9)
        );

        var belt = Belt.create("Default", 10, seatSpecs);

        assertAll("Seats created",
                () -> assertEquals(3, belt.getSeats().size()),
                () -> assertTrue(belt.findSeatByPosition(0).isPresent()),
                () -> assertTrue(belt.findSeatByPosition(5).isPresent()),
                () -> assertTrue(belt.findSeatByPosition(9).isPresent()),
                () -> assertTrue(belt.findSeatByPosition(1).isEmpty())
        );

        var seatB = belt.findSeatByPosition(5).orElseThrow();
        assertAll("SeatB fields",
                () -> assertEquals("B", seatB.getLabel()),
                () -> assertEquals(5, seatB.getPositionIndex()),
                () -> assertNotNull(seatB.getId())
        );
    }

    @Test
    @DisplayName("Seat label is normalized (trim + upper) and must be alphanumeric")
    void label_normalization_and_validation() {
        var belt = Belt.create("Default", 10, List.of(
                new SeatSpec(" 1 ", 0),
                new SeatSpec(" 1a ", 1),
                new SeatSpec("i", 2)
        ));

        assertEquals("1", belt.findSeatByPosition(0).orElseThrow().getLabel());
        assertEquals("1A", belt.findSeatByPosition(1).orElseThrow().getLabel());
        assertEquals("I", belt.findSeatByPosition(2).orElseThrow().getLabel());

        assertAll("Invalid labels",
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Belt.create("X", 10, List.of(new SeatSpec("", 0)))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Belt.create("X", 10, List.of(new SeatSpec("  ", 0)))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Belt.create("X", 10, List.of(new SeatSpec("a a", 0)))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Belt.create("X", 10, List.of(new SeatSpec("A-1", 0)))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Belt.create("X", 10, List.of(new SeatSpec("A_1", 0)))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Belt.create("X", 10, List.of(new SeatSpec("A#", 0))))
        );
    }

    @Test
    @DisplayName("Seat positions must be within [0..slotCount-1]")
    void position_in_range() {
        assertAll("Position handling",
                () -> assertDoesNotThrow(() -> Belt.create("Default", 10, List.of(new SeatSpec("A", 0)))),
                () -> assertDoesNotThrow(() -> Belt.create("Default", 10, List.of(new SeatSpec("B", 9)))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Belt.create("Default", 10, List.of(new SeatSpec("C", -1)))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Belt.create("Default", 10, List.of(new SeatSpec("D", 10))))
        );
    }

    @Test
    @DisplayName("Seat labels must be unique within a belt")
    void duplicate_label_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                Belt.create("Default", 10, List.of(
                        new SeatSpec("A", 0),
                        new SeatSpec("a", 1) // normalizes to "A"
                )));
    }

    @Test
    @DisplayName("Seat positions must be unique within a belt")
    void duplicate_position_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                Belt.create("Default", 10, List.of(
                        new SeatSpec("A", 0),
                        new SeatSpec("B", 0)
                )));
    }

    @Test
    @DisplayName("Seat ids are deterministic based on beltId and positionIndex")
    void seat_id_is_deterministic() {
        var belt = Belt.create("Default", 10, List.of(new SeatSpec("A", 3)));

        var seat = belt.findSeatByPosition(3).orElseThrow();
        UUID beltId = belt.getId();

        UUID expected = UUID.nameUUIDFromBytes((beltId + ":SEAT:" + 3).getBytes(StandardCharsets.UTF_8));

        assertEquals(expected, seat.getId());
    }

    @Test
    @DisplayName("getSeats returns an unmodifiable view")
    void seats_are_immutable() {
        var belt = Belt.create("Default", 10, List.of(new SeatSpec("A", 0)));
        assertThrows(UnsupportedOperationException.class, () -> belt.getSeats().add(null));
    }
}
