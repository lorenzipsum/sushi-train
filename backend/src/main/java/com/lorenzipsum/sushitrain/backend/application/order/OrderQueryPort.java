package com.lorenzipsum.sushitrain.backend.application.order;

import com.lorenzipsum.sushitrain.backend.application.view.OrderSummaryView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderQueryPort {
    Page<OrderSummaryView> findOrderSummaries(Pageable pageable);
}
