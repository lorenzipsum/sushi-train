package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FullBeltDto(
        UUID id,
        String name,
        Integer slotCount,
        Integer baseRotationOffset,
        Integer tickIntervalMs,
        Integer speedSlotsPerTick,
        List<BeltSlotDto> slots,
        List<SeatDto> seats,
        Instant offsetStartedAt
) {
}
