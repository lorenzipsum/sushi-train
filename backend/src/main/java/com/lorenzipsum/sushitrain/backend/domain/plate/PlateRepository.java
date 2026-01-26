package com.lorenzipsum.sushitrain.backend.domain.plate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlateRepository {
    Optional<Plate> findById(UUID uuid);

    Plate save(Plate plate);

    List<Plate> findAll();
}
