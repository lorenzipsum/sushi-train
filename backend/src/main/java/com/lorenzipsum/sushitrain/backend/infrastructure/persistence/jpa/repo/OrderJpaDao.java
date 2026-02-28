package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.OrderEntity;
import com.lorenzipsum.sushitrain.backend.domain.common.OrderStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            WITH ranked_open_orders AS (
                SELECT
                    o.id,
                    o.created_at,
                    row_number() OVER (
                        PARTITION BY o.seat_id
                        ORDER BY o.created_at, o.id
                    ) AS rn
                FROM orders o
                WHERE o.status = 'OPEN'
            )
            UPDATE orders o
               SET status = 'CANCELED',
                   closed_at = GREATEST(o.created_at, COALESCE(o.closed_at, now()))
              FROM ranked_open_orders r
             WHERE o.id = r.id
               AND r.rn > 1
               AND o.status = 'OPEN'
            """, nativeQuery = true)
    int closeDuplicateOpenOrdersPerSeat();

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
