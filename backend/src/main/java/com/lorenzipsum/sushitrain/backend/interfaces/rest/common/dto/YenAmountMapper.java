package com.lorenzipsum.sushitrain.backend.interfaces.rest.common.dto;

import com.lorenzipsum.sushitrain.backend.domain.common.YenAmount;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface YenAmountMapper {
    default Integer map(YenAmount value) {
        return value == null ? null : value.amount();
    }
}
