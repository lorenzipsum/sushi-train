package com.lorenzipsum.sushitrain.backend.infrastructure.scheduling;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltSlotJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.PlateJpaDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
class DataIntegrityRepairService {

    private final PlateJpaDao plateJpaDao;
    private final BeltSlotJpaDao beltSlotJpaDao;

    DataIntegrityRepairService(PlateJpaDao plateJpaDao, BeltSlotJpaDao beltSlotJpaDao) {
        this.plateJpaDao = plateJpaDao;
        this.beltSlotJpaDao = beltSlotJpaDao;
    }

    @Transactional
    RepairSummary repairKnownAnomalies() {
        List<UUID> inconsistentPlateIds = plateJpaDao.findOnBeltPlateIdsAlreadyAssignedToOrderLine();
        if (inconsistentPlateIds.isEmpty()) {
            return new RepairSummary(0, 0, 0);
        }

        int clearedSlots = beltSlotJpaDao.clearPlateAssignments(inconsistentPlateIds);
        int markedPicked = plateJpaDao.markPicked(inconsistentPlateIds);

        return new RepairSummary(inconsistentPlateIds.size(), clearedSlots, markedPicked);
    }

    record RepairSummary(
            int detectedPlates,
            int clearedSlots,
            int markedPicked
    ) {
    }
}
