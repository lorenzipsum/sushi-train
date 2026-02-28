package com.lorenzipsum.sushitrain.backend.application.view;

import java.util.UUID;

public record SeatStateView(
        UUID seatId,
        String label,
        int positionIndex,
        boolean isOccupied
) {
}
