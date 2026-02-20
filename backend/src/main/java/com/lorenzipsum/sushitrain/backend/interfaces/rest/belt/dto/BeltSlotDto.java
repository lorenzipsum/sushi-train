package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto;

import java.util.UUID;

public record BeltSlotDto(
        UUID id,
        Integer positionIndex,
        UUID plateId
) {
}
