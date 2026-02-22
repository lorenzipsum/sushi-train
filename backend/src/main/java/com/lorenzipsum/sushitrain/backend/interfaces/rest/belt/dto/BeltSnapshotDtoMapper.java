package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.projection.BeltSlotPlateRow;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BeltSnapshotDtoMapper {

    public BeltSnapshotDto toDto(List<BeltSlotPlateRow> rows) {
        if (rows == null || rows.isEmpty()) return null;

        var first = rows.getFirst();

        var slotDtos = rows.stream().map(r -> new BeltSlotSnapshotDto(
                r.slotId(),
                r.slotPositionIndex(),
                r.plateId() == null ? null : new BeltSlotSnapshotDto.PlateSnapshotDto(
                        r.plateId(),
                        r.menuItemId(),
                        r.menuItemName(),
                        r.plateTier(),
                        r.platePriceAtCreation(),
                        r.plateStatus(),
                        r.plateExpiresAt()
                )
        )).toList();

        return new BeltSnapshotDto(
                first.beltId(),
                first.beltName(),
                first.beltSlotCount(),
                first.beltBaseRotationOffset(),
                first.beltOffsetStartedAt(),
                first.beltTickIntervalMs(),
                first.beltSpeedSlotsPerTick(),
                slotDtos
        );
    }
}