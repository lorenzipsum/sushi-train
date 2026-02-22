package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BeltSnapshotDto(
        UUID beltId,
        String beltName,
        Integer beltSlotCount,
        Integer beltBaseRotationOffset,
        Instant beltOffsetStartedAt,
        Integer beltTickIntervalMs,
        Integer beltSpeedSlotsPerTick,
        List<BeltSlotSnapshotDto> slots
) {}