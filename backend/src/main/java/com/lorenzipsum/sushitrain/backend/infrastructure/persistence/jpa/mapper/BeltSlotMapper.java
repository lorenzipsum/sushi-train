package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper;

import com.lorenzipsum.sushitrain.backend.domain.belt.BeltSlot;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltSlotEntity;
import org.springframework.stereotype.Component;

/**
 * Maps between domain BeltSlot and JPA BeltSlotEntity.
 */
@Component
public class BeltSlotMapper {

    public BeltSlot toDomain(BeltSlotEntity e) {
        if (e == null) return null;
        return new BeltSlot(
                e.getId(),
                e.getBelt().getId(),
                e.getPositionIndex(),
                e.getPlateId()
        );
    }

    public BeltSlotEntity toEntity(BeltSlot d, BeltEntity belt) {
        if (d == null) return null;
        return new BeltSlotEntity(
                d.getId(),
                d.getPositionIndex(),
                belt,
                d.getPlateId()
        );
    }
}
