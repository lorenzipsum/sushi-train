package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.application.order.OrderQueryPort;
import com.lorenzipsum.sushitrain.backend.application.view.OrderLineView;
import com.lorenzipsum.sushitrain.backend.application.view.OrderSummaryView;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.OrderJpaDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JpaOrderQueryAdapter implements OrderQueryPort {
    private final OrderJpaDao orderJpaDao;

    public JpaOrderQueryAdapter(OrderJpaDao orderJpaDao) {
        this.orderJpaDao = orderJpaDao;
    }

    @Override
    public Page<OrderSummaryView> findOrderSummaries(Pageable pageable) {
        var headerPage = orderJpaDao.findOrderHeaders(pageable);
        var orderIds = headerPage.getContent().stream()
                .map(OrderJpaDao.OrderHeaderView::getOrderId)
                .toList();

        Map<UUID, java.util.List<OrderLineView>> linesByOrderId = orderIds.isEmpty()
                ? java.util.Map.of()
                : orderJpaDao.findOrderLinesByOrderIds(orderIds).stream()
                .collect(Collectors.groupingBy(
                        OrderJpaDao.OrderLineView::getOrderId,
                        Collectors.mapping(
                                line -> new OrderLineView(line.getMenuItemName(), line.getPlateTier(), line.getPrice()),
                                Collectors.toList()
                        )
                ));

        return headerPage.map(header -> {
            var lines = linesByOrderId.getOrDefault(header.getOrderId(), java.util.List.of());
            int totalPrice = lines.stream().mapToInt(OrderLineView::price).sum();
            return new OrderSummaryView(
                    header.getOrderId(),
                    header.getSeatId(),
                    header.getStatus(),
                    header.getCreatedAt(),
                    header.getClosedAt(),
                    lines,
                    totalPrice
            );
        });
    }
}
