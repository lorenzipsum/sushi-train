package com.lorenzipsum.sushitrain.backend.application.common;

import java.util.UUID;

public class SeatAlreadyOccupiedException extends RuntimeException {
    public SeatAlreadyOccupiedException(UUID seatId) {
        super("Seat already occpuied: " + seatId);
    }
}