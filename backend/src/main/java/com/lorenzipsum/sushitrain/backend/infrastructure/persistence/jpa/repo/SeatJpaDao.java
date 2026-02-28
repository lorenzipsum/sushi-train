package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo;

import com.lorenzipsum.sushitrain.backend.application.view.SeatStateView;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SeatJpaDao extends JpaRepository<SeatEntity, UUID> {

    @Query("""
            SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END
            FROM OrderEntity o
            WHERE o.seat.id = :seatId AND o.status = 'OPEN'
            """)
    boolean isSeatOccupied(UUID seatId);

    @Query("""
            SELECT new com.lorenzipsum.sushitrain.backend.application.view.SeatStateView(
                s.id,
                s.label,
                s.positionIndex,
                CASE WHEN COUNT(o) > 0 THEN true ELSE false END
            )
            FROM SeatEntity s
            LEFT JOIN OrderEntity o ON o.seat.id = s.id AND o.status = 'OPEN'
            WHERE s.belt.id = :beltId
            GROUP BY s.id, s.label, s.positionIndex
            ORDER BY s.positionIndex ASC
            """)
    List<SeatStateView> findSeatStatesByBeltId(UUID beltId);
}
