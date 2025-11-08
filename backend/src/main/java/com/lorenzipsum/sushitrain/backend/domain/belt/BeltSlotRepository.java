package com.lorenzipsum.sushitrain.backend.domain.belt;

import java.util.Optional;
import java.util.UUID;

public interface BeltSlotRepository {
    Optional<BeltSlot> findById(UUID uuid);

    BeltSlot save(BeltSlot beltSlot);
}
