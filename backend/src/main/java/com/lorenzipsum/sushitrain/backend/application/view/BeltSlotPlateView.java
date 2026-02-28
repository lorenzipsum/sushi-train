package com.lorenzipsum.sushitrain.backend.application.view;

import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.common.YenAmount;

import java.time.Instant;
import java.util.UUID;

public record BeltSlotPlateView(
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
        YenAmount platePriceAtCreation,
        PlateStatus plateStatus,
        Instant plateExpiresAt
) {
}
