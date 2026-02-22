package com.lorenzipsum.sushitrain.backend.application.belt;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.projection.BeltSlotPlateRow;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.query.BeltJpaQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BeltService {
    private final BeltRepository repository;
    private final BeltJpaQuery beltJpaQuery;

    public BeltService(BeltRepository repository, BeltJpaQuery beltJpaQuery) {
        this.repository = repository;
        this.beltJpaQuery = beltJpaQuery;
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
}
