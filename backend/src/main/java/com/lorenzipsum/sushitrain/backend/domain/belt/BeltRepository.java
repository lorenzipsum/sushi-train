package com.lorenzipsum.sushitrain.backend.domain.belt;

import java.util.Optional;
import java.util.UUID;

public interface BeltRepository {
    Optional<Belt> findById(UUID uuid);

    Belt save(Belt belt);
}
