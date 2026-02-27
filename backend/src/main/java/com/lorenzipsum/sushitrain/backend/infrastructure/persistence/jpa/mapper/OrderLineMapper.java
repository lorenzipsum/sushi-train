package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper;

import com.lorenzipsum.sushitrain.backend.domain.order.OrderLine;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.OrderEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.OrderLineEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.PlateEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Maps between domain OrderLine and JPA OrderLineEntity.
 */
@Component
public class OrderLineMapper {

    public OrderLine toDomain(OrderLineEntity e, UUID orderId) {
        if (e == null) return null;
        return OrderLine.rehydrate(
                e.getId(),
                e.getPlate().getId(),
                orderId,
                e.getMenuItemNameSnapshot(),
                e.getTierSnapshot(),
                e.getPriceAtPick(),
                e.getPickedAt()
        );
    }

    public OrderLineEntity toEntity(OrderLine d, PlateEntity plate, OrderEntity order) {
        if (d == null) return null;
        return new OrderLineEntity(
                d.getId(),
                plate,
                order,
                d.getMenuItemNameSnapshot(),
                d.getTierSnapshot(),
                d.getPriceAtPick(),
                d.getPickedAt()
        );
    }
}
