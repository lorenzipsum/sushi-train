package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.OrderEntity;
import com.lorenzipsum.sushitrain.backend.domain.common.OrderStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaDao extends JpaRepository<OrderEntity, UUID> {
    @Query(""" 
            SELECT o
            FROM OrderEntity o
            WHERE o.seat.id = :seatId
            AND o.status = 'OPEN'
            """)
    Optional<OrderEntity> findBySeatId(UUID seatId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT o
            FROM OrderEntity o
            WHERE o.seat.id = :seatId
            AND o.status = 'OPEN'
            """)
    Optional<OrderEntity> findBySeatIdForUpdate(@Param("seatId") UUID seatId);

    @Query("""
            SELECT o.id as orderId,
                   o.seat.id as seatId,
                   o.status as status,
                   o.createdAt as createdAt,
                   o.closedAt as closedAt
            FROM OrderEntity o
            """)
    Page<OrderHeaderView> findOrderHeaders(Pageable pageable);

    @Query("""
            SELECT ol.order.id as orderId,
                   ol.menuItemNameSnapshot as menuItemName,
                   ol.tierSnapshot as plateTier,
                   ol.priceAtPick.amount as price
            FROM OrderLineEntity ol
            WHERE ol.order.id in :orderIds
            ORDER BY ol.pickedAt ASC
            """)
    List<OrderLineView> findOrderLinesByOrderIds(@Param("orderIds") List<UUID> orderIds);

    interface OrderHeaderView {
        UUID getOrderId();
        UUID getSeatId();
        OrderStatus getStatus();
        Instant getCreatedAt();
        Instant getClosedAt();
    }

    interface OrderLineView {
        UUID getOrderId();
        String getMenuItemName();
        PlateTier getPlateTier();
        int getPrice();
    }
}
