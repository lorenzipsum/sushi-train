package com.lorenzipsum.sushitrain.backend.application.order;

import java.util.Optional;
import java.util.UUID;

public interface SeatQueryPort {
    Optional<SeatInfo> findSeatById(UUID seatId);

    Optional<SeatInfo> findSeatByIdForUpdate(UUID seatId);

    boolean isSeatOccupied(UUID seatId);

    record SeatInfo(
            UUID seatId,
            UUID beltId,
            String label,
            int positionIndex
    ) {
    }
}
