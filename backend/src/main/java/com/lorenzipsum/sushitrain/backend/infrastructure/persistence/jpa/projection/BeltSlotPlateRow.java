package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.projection;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;

import java.time.Instant;
import java.util.UUID;

public record BeltSlotPlateRow(
        UUID beltId,
        String beltName,
        int beltSlotCount,
        int beltBaseRotationOffset,
        Instant beltOffsetStartedAt,
        int beltTickIntervalMs,
        int beltSpeedSlotsPerTick,
        UUID slotId,
        int slotPositionIndex,
        UUID plateId,
        UUID menuItemId,
        String menuItemName,
        PlateTier plateTier,
        MoneyYen platePriceAtCreation,
        PlateStatus plateStatus,
        Instant plateExpiresAt
) {
}