package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.projection.BeltParamsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BeltJpaDao extends JpaRepository<BeltEntity, UUID> {
    @Query("""
              select b.id as id,
                     b.name as name,
                     b.slotCount as slotCount,
                     b.baseRotationOffset as baseRotationOffset,
                     b.offsetStartedAt as offsetStartedAt,
                     b.tickIntervalMs as tickIntervalMs,
                     b.speedSlotsPerTick as speedSlotsPerTick
                from BeltEntity b
            """)
    List<BeltParamsProjection> findAllBeltsWithParams();

    @Query("""
              select b.id as id,
                     b.name as name,
                     b.slotCount as slotCount,
                     b.baseRotationOffset as baseRotationOffset,
                     b.offsetStartedAt as offsetStartedAt,
                     b.tickIntervalMs as tickIntervalMs,
                     b.speedSlotsPerTick as speedSlotsPerTick
                from BeltEntity b
               where b.id = :id
            """)
    Optional<BeltParamsProjection> findParamsById(UUID id);

    @Modifying
    @Query("""
              update BeltEntity b
                 set b.tickIntervalMs = :tickIntervalMs,
                     b.speedSlotsPerTick = :speedSlotsPerTick,
                     b.baseRotationOffset = :baseRotationOffset,
                     b.offsetStartedAt = :offsetStartedAt
               where b.id = :id
            """)
    int updateParams(UUID id,
                     int tickIntervalMs,
                     int speedSlotsPerTick,
                     int baseRotationOffset,
                     Instant offsetStartedAt);

}
