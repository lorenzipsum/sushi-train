package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper;

import com.lorenzipsum.sushitrain.backend.domain.order.Order;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.OrderEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.OrderLineEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.SeatEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Maps between domain Order and JPA OrderEntity.
 */
@Component
public class OrderMapper {
    private final OrderLineMapper orderLineMapper;

    public OrderMapper(OrderLineMapper orderLineMapper) {
        this.orderLineMapper = orderLineMapper;
    }

    public Order toDomain(OrderEntity e) {
        if (e == null) return null;
        return Order.rehydrate(
                e.getId(),
                e.getSeat().getId(),
                e.getLines().stream().map(orderLineMapper::toDomain).toList(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getClosedAt()
        );
    }

    public OrderEntity toEntity(Order d, SeatEntity seat, List<OrderLineEntity> lines) {
        if (d == null) return null;
        return new OrderEntity(
                d.getId(),
                seat,
                lines,
                d.getStatus(),
                d.getCreatedAt(),
                d.getClosedAt()
        );
    }
}
