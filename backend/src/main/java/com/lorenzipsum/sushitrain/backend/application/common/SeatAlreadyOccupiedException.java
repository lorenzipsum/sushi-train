package com.lorenzipsum.sushitrain.backend.application.common;

import java.util.UUID;

public class SeatAlreadyOccupiedException extends RuntimeException {
    private final UUID seatId;

    public SeatAlreadyOccupiedException(UUID seatId) {
        super("Seat already occupied: " + seatId);
        this.seatId = seatId;
    }

    public UUID seatId() {
        return seatId;
    }
}
