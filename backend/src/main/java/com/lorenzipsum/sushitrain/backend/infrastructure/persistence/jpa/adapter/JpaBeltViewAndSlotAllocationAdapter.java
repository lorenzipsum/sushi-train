package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.application.belt.BeltSlotAllocationCommandPort;
import com.lorenzipsum.sushitrain.backend.application.belt.BeltQueryPort;
import com.lorenzipsum.sushitrain.backend.application.view.BeltSlotPlateView;
import com.lorenzipsum.sushitrain.backend.application.view.SeatStateView;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.query.BeltJpaQuery;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltSlotJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.PlateJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.SeatJpaDao;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class JpaBeltViewAndSlotAllocationAdapter implements BeltQueryPort, BeltSlotAllocationCommandPort {
    private final BeltJpaQuery beltJpaQuery;
    private final BeltSlotJpaDao beltSlotJpaDao;
    private final SeatJpaDao seatJpaDao;
    private final PlateJpaDao plateJpaDao;

    public JpaBeltViewAndSlotAllocationAdapter(BeltJpaQuery beltJpaQuery,
                                               BeltSlotJpaDao beltSlotJpaDao,
                                               SeatJpaDao seatJpaDao,
                                               PlateJpaDao plateJpaDao) {
        this.beltJpaQuery = beltJpaQuery;
        this.beltSlotJpaDao = beltSlotJpaDao;
        this.seatJpaDao = seatJpaDao;
        this.plateJpaDao = plateJpaDao;
    }

    @Override
    public List<BeltSlotPlateView> findBeltSnapshot(UUID beltId) {
        return beltJpaQuery.findBeltSnapshot(beltId).stream()
                .map(row -> new BeltSlotPlateView(
                        row.beltId(),
                        row.beltName(),
                        row.beltSlotCount(),
                        row.beltBaseRotationOffset(),
                        row.beltOffsetStartedAt(),
                        row.beltTickIntervalMs(),
                        row.beltSpeedSlotsPerTick(),
                        row.slotId(),
                        row.slotPositionIndex(),
                        row.plateId(),
                        row.menuItemId(),
                        row.menuItemName(),
                        row.plateTier(),
                        row.platePriceAtCreation(),
                        row.plateStatus(),
                        row.plateExpiresAt()
                ))
                .toList();
    }

    @Override
    public List<SeatStateView> findSeatStates(UUID beltId) {
        return seatJpaDao.findSeatStatesByBeltId(beltId).stream()
                .map(row -> new SeatStateView(row.seatId(), row.label(), row.positionIndex(), row.isOccupied()))
                .toList();
    }

    @Override
    public List<FreeBeltSlot> findFreeSlotsForUpdate(UUID beltId) {
        return beltSlotJpaDao.findFreeSlotsForUpdate(beltId).stream()
                .map(slot -> new FreeBeltSlot(slot.getId(), slot.getPositionIndex()))
                .toList();
    }

    @Override
    public void assignPlateToSlot(UUID slotId, UUID plateId) {
        var slotRef = beltSlotJpaDao.getReferenceById(slotId);
        slotRef.setPlate(plateJpaDao.getReferenceById(plateId));
    }
}
