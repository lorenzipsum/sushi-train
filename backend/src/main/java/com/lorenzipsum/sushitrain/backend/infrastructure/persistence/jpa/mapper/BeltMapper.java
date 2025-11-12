package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper;

import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltEntity;
import org.springframework.stereotype.Component;

/**
 * Maps between domain Belt and JPA BeltEntity.
 */
@Component
public class BeltMapper {

    public Belt toDomain(BeltEntity e) {
        if (e == null) return null;
        return Belt.rehydrate(
                e.getId(),
                e.getName(),
                e.getSlotCount(),
                e.getRotationOffset(),
                e.getTickIntervalMs(),
                e.getSpeedSlotsPerTick()
        );
    }

    public BeltEntity toEntity(Belt d) {
        if (d == null) return null;
        return new BeltEntity(
                d.getId(),
                d.getName(),
                d.getSlotCount(),
                d.getRotationOffset(),
                d.getTickIntervalMs(),
                d.getSpeedSlotsPerTick()
        );
    }
}
