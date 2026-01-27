package com.lorenzipsum.sushitrain.backend.interfaces.rest.menu.dto;

import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

public record MenuItemDto(
        @NotNull UUID id,
        @NotNull String name,
        @NotNull PlateTier defaultTier,
        @NotNull @Positive Integer basePrice,
        @NotNull Instant createdAt
) {
}
