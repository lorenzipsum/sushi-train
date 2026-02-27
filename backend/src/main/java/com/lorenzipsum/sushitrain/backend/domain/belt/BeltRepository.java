package com.lorenzipsum.sushitrain.backend.domain.belt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BeltRepository {
    List<Belt> findAllBelts();

    Optional<Belt> findById(UUID uuid);

    Optional<Belt> findParamsById(UUID id);

    Belt create(Belt belt);

    Belt saveParams(Belt belt);
}
