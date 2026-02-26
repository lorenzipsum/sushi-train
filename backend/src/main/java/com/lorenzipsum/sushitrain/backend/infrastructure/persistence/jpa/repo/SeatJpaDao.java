package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface SeatJpaDao extends JpaRepository<SeatEntity, UUID> {

    @Query("""
            SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END
            FROM OrderEntity o
            WHERE o.seat.id = :seatId AND o.status = 'OPEN'
            """)
    boolean isSeatOccupied(UUID seatId);
}
