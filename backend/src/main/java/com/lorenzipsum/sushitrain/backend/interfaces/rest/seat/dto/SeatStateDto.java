package com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto;

import java.util.UUID;

public record SeatStateDto(
        UUID seatId,
        String label,
        int positionIndex,
        boolean isOccupied
) {
}
