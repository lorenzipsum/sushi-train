package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo;

import java.util.UUID;

public record SeatStateRow(
        UUID seatId,
        String label,
        int positionIndex,
        boolean isOccupied
) {
}
