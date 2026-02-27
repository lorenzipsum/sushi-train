package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
