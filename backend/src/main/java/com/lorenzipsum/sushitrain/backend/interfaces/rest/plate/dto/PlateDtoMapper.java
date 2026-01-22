package com.lorenzipsum.sushitrain.backend.interfaces.rest.plate.dto;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface PlateDtoMapper {
    PlateDto toDto(Plate plate);

    Plate toDomain(CreatePlateRequest createPlateRequest);

    default Integer map(MoneyYen value) {
        return value == null ? null : value.amount();
    }

    default MoneyYen map(Integer value) {
        return value == null ? null : MoneyYen.of(value);
    }

    @ObjectFactory
    default Plate createPlate(CreatePlateRequest request) {
        return Plate.create(
                request.menuItemId(),
                request.tierSnapshot(),
                MoneyYen.of(request.priceAtCreation()),
                request.expiresAt()
        );
    }
}