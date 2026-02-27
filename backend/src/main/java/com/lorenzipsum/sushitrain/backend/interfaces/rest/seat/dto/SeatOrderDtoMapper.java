package com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto;

import com.lorenzipsum.sushitrain.backend.domain.order.Order;
import com.lorenzipsum.sushitrain.backend.domain.order.OrderLine;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.common.dto.YenAmountMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = YenAmountMapper.class)
public interface SeatOrderDtoMapper {
    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "totalPrice", expression = "java(order.total().amount())")
    OrderSummaryDto toSeatOrderDto(Order order);

    @SuppressWarnings("unused")
    @Mapping(target = "menuItemName", source = "menuItemNameSnapshot")
    @Mapping(target = "plateTier", source = "tierSnapshot")
    @Mapping(target = "price", source = "priceAtPick")
    OrderLineDto toOrderLineDto(OrderLine orderLine);
}
