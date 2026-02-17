package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.projection;

import java.time.Instant;
import java.util.UUID;

public interface BeltParamsProjection {
    UUID getId();

    String getName();

    int getSlotCount();

    int getBaseRotationOffset();

    Instant getOffsetStartedAt();

    int getTickIntervalMs();

    int getSpeedSlotsPerTick();
}
