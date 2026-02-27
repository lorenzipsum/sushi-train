package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto;

import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BeltDtoMapper {
    BeltParamsDto toParamsDto(Belt belt);

    FullBeltDto toFullDto(Belt belt);

    BeltDto toDto(Belt belt);
}
