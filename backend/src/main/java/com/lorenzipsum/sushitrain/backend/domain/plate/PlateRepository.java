package com.lorenzipsum.sushitrain.backend.domain.plate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PlateRepository {
    Optional<Plate> findById(UUID uuid);

    Plate save(Plate plate);

    Page<Plate> findAll(Pageable pageable);
}
