package com.lorenzipsum.sushitrain.backend.interfaces.rest.plate;

import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

public record CreatePlateRequest(
        @NotNull
        @Schema(description = "Menu item id the plate refers to",
                example = "11111111-1111-1111-1111-111111111111",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID menuItemId,

        @Schema(description = "Optional tier snapshot. If omitted, service can derive from menu item.",
                example = "RED",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        PlateTier tierSnapshot,

        @Positive
        @Schema(description = "Optional price in Yen at creation time. If omitted, service can derive from menu item.",
                example = "500",
                minimum = "1",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer priceAtCreation,

        @Future
        @Schema(description = "Optional expiration timestamp (UTC). If omitted, service can compute default TTL.",
                example = "2026-01-19T15:22:08.485369100Z",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Instant expiresAt) {
}
