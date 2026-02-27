package com.lorenzipsum.sushitrain.backend.application.common;

import java.util.UUID;

public class SeatNotOccupiedException extends RuntimeException {
    public SeatNotOccupiedException(UUID seatId) {
        super("Seat is not occupied: " + seatId);
    }
}
