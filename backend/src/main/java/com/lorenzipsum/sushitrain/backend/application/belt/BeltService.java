package com.lorenzipsum.sushitrain.backend.application.belt;

import com.lorenzipsum.sushitrain.backend.application.common.NotEnoughFreeSlotsException;
import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.application.plate.PlateService;
import com.lorenzipsum.sushitrain.backend.application.view.BeltSlotPlateView;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.domain.common.YenAmount;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
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
    private final BeltQueryPort beltQueryPort;
    private final BeltSlotAllocationCommandPort beltSlotAllocationPort;
    private final PlateService plateService;
    private final BeltPlacementRules beltPlacementRules;

    public BeltService(BeltRepository repository,
                       BeltQueryPort beltQueryPort,
                       BeltSlotAllocationCommandPort beltSlotAllocationPort,
                       PlateService plateService,
                       BeltPlacementRules beltPlacementRules) {
        this.repository = repository;
        this.beltQueryPort = beltQueryPort;
        this.beltSlotAllocationPort = beltSlotAllocationPort;
        this.plateService = plateService;
        this.beltPlacementRules = beltPlacementRules;
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
    public List<BeltSlotPlateView> getBeltSnapshotRows(UUID id) {
        if (id == null) throw new IllegalArgumentException("id cannot be null");
        var rows = beltQueryPort.findBeltSnapshot(id);
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
        return beltQueryPort.findSeatStates(beltId);
    }

    @Transactional
    public CreatedPlatesResult createPlatesAndPlaceOnBelt(UUID beltId, CreatePlatesCommand request) {
        if (beltId == null) throw new IllegalArgumentException("beltId cannot be null");
        if (request == null) throw new IllegalArgumentException("request cannot be null");
        if (request.menuItemId() == null) throw new IllegalArgumentException("menuItemId cannot be null");

        // Distinguish 404 vs 409 explicitly
        repository.findParamsById(beltId).orElseThrow(() -> new ResourceNotFoundException("Belt", beltId));

        int num = (request.numOfPlates() == null) ? 1 : request.numOfPlates();

        var freeSlots = beltSlotAllocationPort.findFreeSlotsForUpdate(beltId);
        if (freeSlots.size() < num) {
            throw new NotEnoughFreeSlotsException(beltId, num, freeSlots.size());
        }

        var pickedSlots = BeltSlotPlacement.pickSlots(freeSlots, beltPlacementRules.minEmptySlotsBetweenNewPlates(), num);
        if (pickedSlots.size() < num) {
            throw new NotEnoughFreeSlotsException(beltId, num, pickedSlots.size());
        }

        var placed = new ArrayList<CreatedPlatesResult.PlacedPlateView>(num);

        for (var slot : pickedSlots) {
            YenAmount priceAtCreation = request.priceAtCreation() == null
                    ? null
                    : YenAmount.of(request.priceAtCreation());

            Plate plate = plateService.createPlate(
                    request.menuItemId(),
                    request.tierSnapshot(),
                    priceAtCreation,
                    request.expiresAt()
            );

            // mark as ON_BELT and persist the status
            plate.place();
            plateService.save(plate);

            // assign to slot via JPA reference
            beltSlotAllocationPort.assignPlateToSlot(slot.slotId(), plate.getId());

            placed.add(new CreatedPlatesResult.PlacedPlateView(
                    plate.getId(),
                    slot.slotId(),
                    slot.positionIndex(),
                    plate.getMenuItemId(),
                    plate.getExpiresAt()
            ));
        }

        return new CreatedPlatesResult(beltId, placed.size(), placed);
    }
}
