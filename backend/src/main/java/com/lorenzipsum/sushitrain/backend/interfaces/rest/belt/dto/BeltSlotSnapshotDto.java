package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto;

import com.lorenzipsum.sushitrain.backend.domain.common.YenAmount;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;

import java.time.Instant;
import java.util.UUID;

public record BeltSlotSnapshotDto(
        UUID slotId,
        Integer positionIndex,
        PlateSnapshotDto plate
) {
    public record PlateSnapshotDto(
            UUID plateId,
            UUID menuItemId,
            String menuItemName,
            PlateTier tier,
            YenAmount priceAtCreation,
            PlateStatus status,
            Instant expiresAt
    ) {
    }
}