package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreatedPlatesOnBeltResponse(
        UUID beltId,
        int createdCount,
        List<PlacedPlateDto> placedPlates
) {
    public record PlacedPlateDto(
            UUID plateId,
            UUID slotId,
            int slotPositionIndex,
            UUID menuItemId,
            Instant expiresAt
    ) {
    }
}