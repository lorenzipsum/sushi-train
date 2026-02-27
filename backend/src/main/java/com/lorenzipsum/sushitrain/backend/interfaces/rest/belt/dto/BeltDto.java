package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto;

import java.time.Instant;
import java.util.UUID;

public record BeltDto(
        UUID id,
        String name,
        Integer slotCount,
        Integer baseRotationOffset,
        Integer tickIntervalMs,
        Integer speedSlotsPerTick,
        Instant offsetStartedAt
) {
}
