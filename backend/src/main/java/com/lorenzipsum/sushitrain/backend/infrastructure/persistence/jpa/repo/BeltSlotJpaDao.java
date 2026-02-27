package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltSlotEntity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BeltSlotJpaDao extends JpaRepository<BeltSlotEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "2000")})
    @Query("""
            select s
              from BeltSlotEntity s
             where s.belt.id = :beltId
               and s.plate is null
             order by s.positionIndex asc
            """)
    List<BeltSlotEntity> findFreeSlotsForUpdate(UUID beltId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update BeltSlotEntity s
              set s.plate = null
            where s.plate.id in :plateIds
           """)
    @SuppressWarnings("UnusedReturnValue")
    int clearPlateAssignments(@Param("plateIds") List<UUID> plateIds);
}