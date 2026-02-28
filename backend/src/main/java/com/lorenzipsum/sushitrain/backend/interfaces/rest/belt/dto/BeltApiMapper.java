package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto;

import com.lorenzipsum.sushitrain.backend.application.belt.CreatePlatesCommand;
import com.lorenzipsum.sushitrain.backend.application.belt.CreatedPlatesResult;
import com.lorenzipsum.sushitrain.backend.application.view.BeltSlotPlateView;
import com.lorenzipsum.sushitrain.backend.application.view.SeatStateView;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.SeatStateDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BeltApiMapper {
    BeltParamsDto toParamsDto(Belt belt);

    FullBeltDto toFullDto(Belt belt);

    BeltDto toDto(Belt belt);

    CreatePlatesCommand toCommand(CreatePlateAndPlaceOnBeltRequest request);

    CreatedPlatesOnBeltResponse toResponse(CreatedPlatesResult result);

    List<SeatStateDto> toSeatStateDtos(List<SeatStateView> views);

    default BeltSnapshotDto toSnapshotDto(List<BeltSlotPlateView> rows) {
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
