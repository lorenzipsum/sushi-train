package com.lorenzipsum.sushitrain.backend.application.belt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreatedPlatesResult(
        UUID beltId,
        int createdCount,
        List<PlacedPlateView> placedPlates
) {
    public record PlacedPlateView(
            UUID plateId,
            UUID slotId,
            int slotPositionIndex,
            UUID menuItemId,
            Instant expiresAt
    ) {
    }
}
