package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.application.plate.PlateExpiryCommandPort;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltSlotJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.PlateJpaDao;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class JpaPlateExpiryAdapter implements PlateExpiryCommandPort {
    private final PlateJpaDao plateJpaDao;
    private final BeltSlotJpaDao beltSlotJpaDao;

    public JpaPlateExpiryAdapter(PlateJpaDao plateJpaDao, BeltSlotJpaDao beltSlotJpaDao) {
        this.plateJpaDao = plateJpaDao;
        this.beltSlotJpaDao = beltSlotJpaDao;
    }

    @Override
    public List<UUID> findExpiredPlateIds(List<PlateStatus> statuses, Instant now) {
        return plateJpaDao.findExpiredPlateIds(statuses, now);
    }

    @Override
    public int markExpired(List<UUID> plateIds, List<PlateStatus> allowedCurrentStatuses) {
        return plateJpaDao.markExpired(plateIds, allowedCurrentStatuses);
    }

    @Override
    public void clearPlateAssignments(List<UUID> plateIds) {
        beltSlotJpaDao.clearPlateAssignments(plateIds);
    }
}
