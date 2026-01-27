package com.lorenzipsum.sushitrain.backend.interfaces.rest.common.dto;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MoneyYenMapper {
    default Integer map(MoneyYen value) {
        return value == null ? null : value.amount();
    }
}
