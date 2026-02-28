package com.lorenzipsum.sushitrain.backend.infrastructure.scheduling;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltSlotJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.OrderJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.PlateJpaDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
class DataIntegrityRepairService {

    private final PlateJpaDao plateJpaDao;
    private final BeltSlotJpaDao beltSlotJpaDao;
    private final OrderJpaDao orderJpaDao;

    DataIntegrityRepairService(PlateJpaDao plateJpaDao, BeltSlotJpaDao beltSlotJpaDao, OrderJpaDao orderJpaDao) {
        this.plateJpaDao = plateJpaDao;
        this.beltSlotJpaDao = beltSlotJpaDao;
        this.orderJpaDao = orderJpaDao;
    }

    @Transactional
    RepairSummary repairKnownAnomalies() {
        int duplicateOpenOrdersClosed = orderJpaDao.closeDuplicateOpenOrdersPerSeat();

        List<UUID> inconsistentPlateIds = plateJpaDao.findOnBeltPlateIdsAlreadyAssignedToOrderLine();
        if (inconsistentPlateIds.isEmpty()) {
            return new RepairSummary(0, 0, 0, duplicateOpenOrdersClosed);
        }

        int clearedSlots = beltSlotJpaDao.clearPlateAssignments(inconsistentPlateIds);
        int markedPicked = plateJpaDao.markPicked(inconsistentPlateIds);

        return new RepairSummary(inconsistentPlateIds.size(), clearedSlots, markedPicked, duplicateOpenOrdersClosed);
    }

    record RepairSummary(
            int detectedPlates,
            int clearedSlots,
            int markedPicked,
            int duplicateOpenOrdersClosed
    ) {
    }
}
