package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper;

import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.MenuItemEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.PlateEntity;
import org.springframework.stereotype.Component;

/**
 * Maps between domain Plate and JPA PlateEntity.
 */
@Component
public class PlateMapper {

    public Plate toDomain(PlateEntity e) {
        if (e == null) return null;
        return Plate.rehydrate(
                e.getId(),
                e.getMenuItem().getId(),
                e.getTierSnapshot(),
                e.getPriceAtCreation(),
                e.getCreatedAt(),
                e.getExpiresAt(),
                e.getStatus()
        );
    }

    public PlateEntity toEntity(Plate d, MenuItemEntity menuItemEntity) {
        if (d == null) return null;
        return new PlateEntity(
                d.getId(),
                menuItemEntity,
                d.getTierSnapshot(),
                d.getPriceAtCreation(),
                d.getCreatedAt(),
                d.getExpiresAt(),
                d.getStatus()
        );
    }
}
