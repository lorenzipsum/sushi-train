package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper;

import com.lorenzipsum.sushitrain.backend.domain.belt.Seat;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.SeatEntity;
import org.springframework.stereotype.Component;

/**
 * Maps between domain Seat and JPA SeatEntity.
 */
@Component
public class SeatMapper {

    public Seat toDomain(SeatEntity e) {
        if (e == null) return null;
        return Seat.rehydrate(
                e.getId(),
                e.getLabel(),
                e.getPositionIndex()
        );
    }

    public SeatEntity toEntity(Seat d, BeltEntity beltEntity) {
        if (d == null) return null;
        return new SeatEntity(
                d.getId(),
                d.getLabel(),
                beltEntity,
                d.getPositionIndex()
        );
    }
}
