package com.lorenzipsum.sushitrain.backend.interfaces.rest.plate.dto;

import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.common.dto.YenAmountMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = YenAmountMapper.class)
public interface PlateDtoMapper {
    PlateDto toDto(Plate plate);
}