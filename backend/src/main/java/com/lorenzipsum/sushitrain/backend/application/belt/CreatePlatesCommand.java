package com.lorenzipsum.sushitrain.backend.application.belt;

import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;

import java.time.Instant;
import java.util.UUID;

public record CreatePlatesCommand(
        UUID menuItemId,
        Integer numOfPlates,
        PlateTier tierSnapshot,
        Integer priceAtCreation,
        Instant expiresAt
) {
}
