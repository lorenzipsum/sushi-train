package com.lorenzipsum.sushitrain.backend.application.plate;

import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltSlotJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.PlateJpaDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PlateExpiryService {

    private final PlateJpaDao plateJpaDao;
    private final BeltSlotJpaDao beltSlotJpaDao;

    public PlateExpiryService(PlateJpaDao plateJpaDao, BeltSlotJpaDao beltSlotJpaDao) {
        this.plateJpaDao = plateJpaDao;
        this.beltSlotJpaDao = beltSlotJpaDao;
    }

    /**
     * Idempotent:
     * - if a plate is already EXPIRED, nothing happens
     * - if a plate is not on a belt slot anymore, belt slot update affects 0 rows and that's fine
     */
    @Transactional
    public int expirePlatesNow() {
        Instant now = Instant.now();

        List<UUID> expiredPlateIds = plateJpaDao.findExpiredPlateIds(
                List.of(PlateStatus.CREATED, PlateStatus.ON_BELT),
                now
        );

        if (expiredPlateIds.isEmpty()) {
            return 0;
        }

        // TODO (batching): If the system grows (many belts / many plates), implement process in chunks (e.g. 500 ids at a time)

        // 1) mark plates as EXPIRED (only if they are still CREATED/ON_BELT, keeps it idempotent)
        int updatedPlates = plateJpaDao.markExpired(
                expiredPlateIds,
                List.of(PlateStatus.CREATED, PlateStatus.ON_BELT)
        );

        // 2) remove plates from belt slots (if assigned)
        beltSlotJpaDao.clearPlateAssignments(expiredPlateIds);

        return updatedPlates;
    }
}