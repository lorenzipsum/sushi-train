package com.lorenzipsum.sushitrain.backend.domain.seat;

import java.util.Optional;
import java.util.UUID;

public interface SeatRepository {
    Optional<Seat> findById(UUID uuid);

    Seat save(Seat seat);
}
