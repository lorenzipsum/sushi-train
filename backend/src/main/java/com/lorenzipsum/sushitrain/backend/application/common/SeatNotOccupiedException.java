package com.lorenzipsum.sushitrain.backend.application.common;

import java.util.UUID;

public class SeatNotOccupiedException extends RuntimeException {
    private final UUID seatId;

    public SeatNotOccupiedException(UUID seatId) {
        super("Seat is not occupied: " + seatId);
        this.seatId = seatId;
    }

    public UUID seatId() {
        return seatId;
    }
}
