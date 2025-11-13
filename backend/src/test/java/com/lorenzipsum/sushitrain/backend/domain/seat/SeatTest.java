package com.lorenzipsum.sushitrain.backend.domain.seat;

import com.lorenzipsum.sushitrain.backend.domain.TestData;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeatTest {
    private final Belt belt = TestData.defaultBelt();

    @Test
    @DisplayName("Seat creation has sane defaults")
    void create_ok() {
        var seat = Seat.create("1", belt.getId(), 5);

        assertAll("Asserting sane defaults for Seat creation",
                () -> assertNotNull(seat.getId()),
                () -> assertEquals("1", seat.getLabel()),
                () -> assertEquals(belt.getId(), seat.getBeltId()),
                () -> assertEquals(5, seat.getSeatPositionIndex())
        );
    }

    @Test
    @DisplayName("Seat creation handles null")
    void create_not_ok() {
        assertAll("Asserting Seat creation handles null",
                () -> assertThrows(IllegalArgumentException.class, () -> Seat.create(null, belt.getId(), 5)),
                () -> assertThrows(IllegalArgumentException.class, () -> Seat.create("1", null, 5))
        );
    }

    @Test
    @DisplayName("Seat creation handles empty label")
    void create_label_not_ok() {
        assertAll("Asserting Seat creation empty label",
                () -> assertThrows(IllegalArgumentException.class, () -> Seat.create("", belt.getId(), 5)),
                () -> assertThrows(IllegalArgumentException.class, () -> Seat.create("  ", belt.getId(), 5)),
                () -> assertThrows(IllegalArgumentException.class, () -> Seat.create("a a", belt.getId(), 5)),
                () -> assertThrows(IllegalArgumentException.class, () -> Seat.create("A-1", belt.getId(), 0)),
                () -> assertThrows(IllegalArgumentException.class, () -> Seat.create("A_1", belt.getId(), 0)),
                () -> assertThrows(IllegalArgumentException.class, () -> Seat.create("A#", belt.getId(), 0))
        );
    }

    @Test
    @DisplayName("Seat creation handles seat position index out of range")
    void create_handles_seat_position_index() {
        assertAll("Asserting correct handling Seat position index",
                () -> assertDoesNotThrow(() -> Seat.create("A", belt.getId(), 0)),
                () -> assertDoesNotThrow(() -> Seat.create("B", belt.getId(), 9)),
                () -> assertThrows(IllegalArgumentException.class, () -> Seat.create("C", belt.getId(), -1))
        );
    }

    @Test
    @DisplayName("Seat label handling")
    void create_trim_ok() {
        assertAll("Asserting seat label handling",
                () -> assertEquals("1", Seat.create(" 1 ", belt.getId(), 5).getLabel()),
                () -> assertEquals("1A", Seat.create(" 1a ", belt.getId(), 5).getLabel()),
                () -> assertEquals("1A", Seat.create(" 1A ", belt.getId(), 5).getLabel()),
                () -> assertEquals("A", Seat.create("a", belt.getId(), 5).getLabel()),
                () -> assertEquals("AA", Seat.create("aa", belt.getId(), 5).getLabel()),
                () -> assertEquals("I", Seat.create("i", belt.getId(), 0).getLabel())
        );
    }
}