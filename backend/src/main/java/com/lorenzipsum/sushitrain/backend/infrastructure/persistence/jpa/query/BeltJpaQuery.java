package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.query;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.projection.BeltSlotPlateRow;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BeltJpaQuery extends Repository<BeltEntity, UUID> {

    @Query("""
        SELECT new com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.projection.BeltSlotPlateRow(
            b.id, b.name, b.slotCount, b.baseRotationOffset, b.offsetStartedAt, b.tickIntervalMs, b.speedSlotsPerTick,
            bs.id, bs.positionIndex,
            p.id, mi.id, mi.name, p.tierSnapshot, p.priceAtCreation, p.status, p.expiresAt
        )
        FROM BeltEntity b
        JOIN b.slots bs
        LEFT JOIN bs.plate p
        LEFT JOIN p.menuItem mi
        WHERE b.id = :beltId
        ORDER BY bs.positionIndex ASC
        """)
    List<BeltSlotPlateRow> findBeltSnapshot(@Param("beltId") UUID beltId);
}