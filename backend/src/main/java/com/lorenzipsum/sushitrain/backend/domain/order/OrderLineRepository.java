package com.lorenzipsum.sushitrain.backend.domain.order;

import java.util.Optional;
import java.util.UUID;

public interface OrderLineRepository {
    Optional<OrderLine> findById(UUID uuid);

    OrderLine save(OrderLine line);
}
