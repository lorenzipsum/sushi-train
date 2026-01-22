package com.lorenzipsum.sushitrain.backend.interfaces.rest.plate.dto;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlateDtoMapper {
    PlateDto toDto(Plate plate);

    default Integer map(MoneyYen value) {
        return value == null ? null : value.amount();
    }
}