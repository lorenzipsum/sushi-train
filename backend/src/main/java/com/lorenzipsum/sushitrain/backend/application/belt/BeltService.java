package com.lorenzipsum.sushitrain.backend.application.belt;

import com.lorenzipsum.sushitrain.backend.application.common.NotEnoughFreeSlotsException;
import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.application.plate.PlateService;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.domain.common.YenAmount;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.infrastructure.config.BeltPlacementProperties;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.projection.BeltSlotPlateRow;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.query.BeltJpaQuery;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltSlotJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.PlateJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.SeatJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.SeatStateRow;
import com.lorenzipsum.sushitrain.backend.application.view.SeatStateView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BeltService {

    private final BeltRepository repository;
    private final BeltJpaQuery beltJpaQuery;

    private final BeltSlotJpaDao beltSlotJpaDao;
    private final SeatJpaDao seatJpaDao;
    private final PlateService plateService;
    private final PlateJpaDao plateJpaDao;
    private final BeltPlacementProperties props;

    public BeltService(BeltRepository repository,
                       BeltJpaQuery beltJpaQuery,
                       BeltSlotJpaDao beltSlotJpaDao,
                       SeatJpaDao seatJpaDao,
                       PlateService plateService,
                       PlateJpaDao plateJpaDao, BeltPlacementProperties props) {
        this.repository = repository;
        this.beltJpaQuery = beltJpaQuery;
        this.beltSlotJpaDao = beltSlotJpaDao;
        this.seatJpaDao = seatJpaDao;
        this.plateService = plateService;
        this.plateJpaDao = plateJpaDao;
        this.props = props;
    }

    @Transactional
    public Belt updateBeltParameters(UUID id, Integer tickIntervalMs, Integer speedSlotsPerTick) {
        if (tickIntervalMs == null && speedSlotsPerTick == null) {
            throw new IllegalArgumentException("At least one parameter must be provided for update.");
        }

        Belt belt = repository.findParamsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Belt", id));

        Instant now = Instant.now();
        if (tickIntervalMs != null) belt.setTickIntervalMs(tickIntervalMs, now);
        if (speedSlotsPerTick != null) belt.setSpeedSlotsPerTick(speedSlotsPerTick, now);

        return repository.saveParams(belt);
    }

    @Transactional(readOnly = true)
    public Belt getBelt(UUID id) {
        if (id == null) throw new IllegalArgumentException("id cannot be null");
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Belt", id));
    }

    @Transactional(readOnly = true)
    public List<BeltSlotPlateRow> getBeltSnapshotRows(UUID id) {
        if (id == null) throw new IllegalArgumentException("id cannot be null");
        var rows = beltJpaQuery.findBeltSnapshot(id);
        if (rows == null || rows.isEmpty()) throw new ResourceNotFoundException("Belt", id);
        return rows;
    }

    @Transactional(readOnly = true)
    public List<Belt> getAllBelts() {
        return repository.findAllBelts();
    }

    @Transactional(readOnly = true)
    public List<SeatStateView> getSeatStates(UUID beltId) {
        if (beltId == null) throw new IllegalArgumentException("beltId cannot be null");
        repository.findParamsById(beltId).orElseThrow(() -> new ResourceNotFoundException("Belt", beltId));
        return seatJpaDao.findSeatStatesByBeltId(beltId).stream()
                .map(this::toSeatStateView)
                .toList();
    }

    @Transactional
    public CreatedPlatesResult createPlatesAndPlaceOnBelt(UUID beltId, CreatePlatesCommand request) {
        if (beltId == null) throw new IllegalArgumentException("beltId cannot be null");
        if (request == null) throw new IllegalArgumentException("request cannot be null");
        if (request.menuItemId() == null) throw new IllegalArgumentException("menuItemId cannot be null");

        // Distinguish 404 vs 409 explicitly
        repository.findParamsById(beltId).orElseThrow(() -> new ResourceNotFoundException("Belt", beltId));

        int num = (request.numOfPlates() == null) ? 1 : request.numOfPlates();

        var freeSlots = beltSlotJpaDao.findFreeSlotsForUpdate(beltId);
        if (freeSlots.size() < num) {
            throw new NotEnoughFreeSlotsException(beltId, num, freeSlots.size());
        }

        var pickedSlots = BeltSlotPlacement.pickSlots(freeSlots, props.minEmptySlotsBetweenNewPlates(), num);
        if (pickedSlots.size() < num) {
            throw new NotEnoughFreeSlotsException(beltId, num, pickedSlots.size());
        }

        var placed = new ArrayList<CreatedPlatesResult.PlacedPlateView>(num);

        for (var slot : pickedSlots) {
            Plate plate = plateService.createPlate(
                    request.menuItemId(),
                    request.tierSnapshot(),
                    YenAmount.of(request.priceAtCreation()),
                    request.expiresAt()
            );

            // mark as ON_BELT and persist the status
            plate.place();
            plateService.save(plate);

            // assign to slot via JPA reference
            slot.setPlate(plateJpaDao.getReferenceById(plate.getId()));

            placed.add(new CreatedPlatesResult.PlacedPlateView(
                    plate.getId(),
                    slot.getId(),
                    slot.getPositionIndex(),
                    plate.getMenuItemId(),
                    plate.getExpiresAt()
            ));
        }

        return new CreatedPlatesResult(beltId, placed.size(), placed);
    }

    private SeatStateView toSeatStateView(SeatStateRow row) {
        return new SeatStateView(row.seatId(), row.label(), row.positionIndex(), row.isOccupied());
    }
}
