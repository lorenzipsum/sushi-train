package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.SeatEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatJpaDao extends JpaRepository<SeatEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM SeatEntity s
            WHERE s.id = :seatId
            """)
    Optional<SeatEntity> findByIdForUpdate(UUID seatId);

    @Query("""
            SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END
            FROM OrderEntity o
            WHERE o.seat.id = :seatId AND o.status = 'OPEN'
            """)
    boolean isSeatOccupied(UUID seatId);

    @Query("""
            SELECT new com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.SeatStateRow(
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
    List<SeatStateRow> findSeatStatesByBeltId(UUID beltId);
}
