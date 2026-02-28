package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto;

import com.lorenzipsum.sushitrain.backend.application.belt.CreatePlatesCommand;
import com.lorenzipsum.sushitrain.backend.application.belt.CreatedPlatesResult;
import com.lorenzipsum.sushitrain.backend.application.view.SeatStateView;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.SeatStateDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BeltPlatesDtoMapper {
    CreatePlatesCommand toCommand(CreatePlateAndPlaceOnBeltRequest request);

    CreatedPlatesOnBeltResponse toResponse(CreatedPlatesResult result);

    List<SeatStateDto> toSeatStateDtos(List<SeatStateView> views);
}
