package com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto;

import com.lorenzipsum.sushitrain.backend.application.view.OrderLineView;
import com.lorenzipsum.sushitrain.backend.application.view.OrderSummaryView;
import com.lorenzipsum.sushitrain.backend.application.view.SeatOrderView;
import com.lorenzipsum.sushitrain.backend.application.view.SeatStateView;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SeatOrderDtoMapper {
    SeatStateDto toSeatStateDto(SeatStateView view);

    SeatOrderDto toSeatOrderDto(SeatOrderView view);

    OrderSummaryDto toOrderSummaryDto(OrderSummaryView view);

    OrderLineDto toOrderLineDto(OrderLineView view);
}
