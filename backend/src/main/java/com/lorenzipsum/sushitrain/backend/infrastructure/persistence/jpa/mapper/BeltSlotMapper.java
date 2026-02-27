package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper;

import com.lorenzipsum.sushitrain.backend.domain.belt.BeltSlot;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltSlotEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.PlateEntity;
import org.springframework.stereotype.Component;

@Component
public class BeltSlotMapper {

    public BeltSlot toDomain(BeltSlotEntity e) {
        if (e == null) return null;
        return new BeltSlot(
                e.getId(),
                e.getPositionIndex(),
                e.getPlate() == null ? null : e.getPlate().getId()
        );
    }

    public BeltSlotEntity toEntity(BeltSlot d, BeltEntity belt, PlateEntity plate) {
        if (d == null) return null;
        return new BeltSlotEntity(
                d.getId(),
                d.getPositionIndex(),
                belt,
                plate
        );
    }
}

