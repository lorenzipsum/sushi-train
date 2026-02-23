package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto;

import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.validation.MaxNewPlatesPerRequest;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreatePlateAndPlaceOnBeltRequest(
        @NotNull UUID menuItemId,
        @Min(1) @MaxNewPlatesPerRequest Integer numOfPlates,
        PlateTier tierSnapshot,
        @Min(0)
        Integer priceAtCreation,
        @Future Instant expiresAt
) {
}