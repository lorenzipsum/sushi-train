package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper;

import com.lorenzipsum.sushitrain.backend.domain.order.Order;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.OrderEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.SeatEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    private final OrderLineMapper orderLineMapper;

    public OrderMapper(OrderLineMapper orderLineMapper) {
        this.orderLineMapper = orderLineMapper;
    }

    public Order toDomain(OrderEntity e) {
        if (e == null) return null;

        var orderId = e.getId();

        return Order.rehydrate(
                orderId,
                e.getSeat().getId(),
                e.getLines().stream().map(orderLine ->
                        orderLineMapper.toDomain(orderLine, orderId)).toList(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getClosedAt()
        );
    }

    public OrderEntity toEntity(Order d, SeatEntity seat) {
        if (d == null) return null;
        return new OrderEntity(
                d.getId(),
                seat,
                d.getStatus(),
                d.getCreatedAt(),
                d.getClosedAt()
        );
    }
}
