package com.lorenzipsum.sushitrain.backend.application.belt;

import com.lorenzipsum.sushitrain.backend.application.common.NotEnoughFreeSlotsException;
import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.application.plate.PlateService;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.domain.common.YenAmount;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.projection.BeltSlotPlateRow;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.query.BeltJpaQuery;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltSlotJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.PlateJpaDao;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto.CreatePlateAndPlaceOnBeltRequest;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto.CreatedPlatesOnBeltResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BeltService {
    private static final int MIN_GAP_SLOTS = 5;

    private final BeltRepository repository;
    private final BeltJpaQuery beltJpaQuery;

    private final BeltSlotJpaDao beltSlotJpaDao;
    private final PlateService plateService;
    private final PlateJpaDao plateJpaDao;

    public BeltService(BeltRepository repository,
                       BeltJpaQuery beltJpaQuery,
                       BeltSlotJpaDao beltSlotJpaDao,
                       PlateService plateService,
                       PlateJpaDao plateJpaDao) {
        this.repository = repository;
        this.beltJpaQuery = beltJpaQuery;
        this.beltSlotJpaDao = beltSlotJpaDao;
        this.plateService = plateService;
        this.plateJpaDao = plateJpaDao;
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

    @Transactional
    public CreatedPlatesOnBeltResponse createPlatesAndPlaceOnBelt(UUID beltId, CreatePlateAndPlaceOnBeltRequest request) {
        if (beltId == null) throw new IllegalArgumentException("beltId cannot be null");
        if (request == null) throw new IllegalArgumentException("request cannot be null");
        if (request.menuItemId() == null) throw new IllegalArgumentException("menuItemId cannot be null");

        // Distinguish 404 vs 409 explicitly
        repository.findParamsById(beltId).orElseThrow(() -> new ResourceNotFoundException("Belt", beltId));

        int num = (request.numOfPlates() == null) ? 1 : request.numOfPlates();

        // Option A: lock free slots (plate_id is null)
        var freeSlots = beltSlotJpaDao.findFreeSlotsForUpdate(beltId);
        if (freeSlots.size() < num) {
            throw new NotEnoughFreeSlotsException(beltId, num, freeSlots.size());
        }

        // Distribution rule: keep >= 5 slots between placements
        var pickedSlots = BeltSlotPlacement.pickSlots(freeSlots, MIN_GAP_SLOTS, num);
        if (pickedSlots.size() < num) {
            throw new NotEnoughFreeSlotsException(beltId, num, pickedSlots.size());
        }

        var placed = new ArrayList<CreatedPlatesOnBeltResponse.PlacedPlateDto>(num);

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

            placed.add(new CreatedPlatesOnBeltResponse.PlacedPlateDto(
                    plate.getId(),
                    slot.getId(),
                    slot.getPositionIndex(),
                    plate.getMenuItemId(),
                    plate.getExpiresAt()
            ));
        }

        return new CreatedPlatesOnBeltResponse(beltId, placed.size(), placed);
    }
}