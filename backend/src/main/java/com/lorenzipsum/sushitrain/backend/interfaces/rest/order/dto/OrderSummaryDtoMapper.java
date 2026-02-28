package com.lorenzipsum.sushitrain.backend.interfaces.rest.order.dto;

import com.lorenzipsum.sushitrain.backend.application.view.OrderSummaryView;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderSummaryDtoMapper {
    OrderSummaryDto toDto(OrderSummaryView view);
}
