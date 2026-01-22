package com.lorenzipsum.sushitrain.backend.interfaces.rest.plate.dto;

import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

public record PlateDto(
        @NotNull UUID id,
        @NotNull UUID menuItemId,
        @NotNull PlateTier tierSnapshot,
        @NotNull @Positive Integer priceAtCreation,
        @NotNull Instant createdAt,
        @NotNull Instant expiresAt,
        @NotNull PlateStatus status) {
}


