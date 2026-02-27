package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo;

import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.PlateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PlateJpaDao extends JpaRepository<PlateEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
              from PlateEntity p
             where p.id = :id
            """)
    java.util.Optional<PlateEntity> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            select p.id
              from PlateEntity p
             where p.status in :statuses
               and p.expiresAt <= :now
            """)
    List<UUID> findExpiredPlateIds(
            @Param("statuses") List<PlateStatus> statuses,
            @Param("now") Instant now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update PlateEntity p
               set p.status = com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus.EXPIRED
             where p.id in :plateIds
               and p.status in :allowedCurrentStatuses
            """)
    int markExpired(
            @Param("plateIds") List<UUID> plateIds,
            @Param("allowedCurrentStatuses") List<PlateStatus> allowedCurrentStatuses
    );

    @Query("""
            select p.id
              from PlateEntity p
             where p.status = com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus.ON_BELT
               and exists (
                    select 1
                      from OrderLineEntity ol
                     where ol.plate.id = p.id
               )
            """)
    List<UUID> findOnBeltPlateIdsAlreadyAssignedToOrderLine();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update PlateEntity p
               set p.status = com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus.PICKED
             where p.id in :plateIds
               and p.status = com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus.ON_BELT
            """)
    int markPicked(@Param("plateIds") List<UUID> plateIds);
}
