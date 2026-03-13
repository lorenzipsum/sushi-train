package com.lorenzipsum.sushitrain.backend.interfaces.rest.order.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;

public class PagedOrderSummaryDto extends PagedModel<OrderSummaryDto> {
    public PagedOrderSummaryDto(Page<OrderSummaryDto> page) {
        super(page);
    }
}
