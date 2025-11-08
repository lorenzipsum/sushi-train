package com.lorenzipsum.sushitrain.backend.domain.order;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Optional<Order> findById(UUID uuid);

    Order save(Order order);
}
