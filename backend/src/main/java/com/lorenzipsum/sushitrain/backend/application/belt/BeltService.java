package com.lorenzipsum.sushitrain.backend.application.belt;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class BeltService {
    private final BeltRepository repository;

    public BeltService(BeltRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Belt updateBeltParameters(UUID id, Integer tickIntervalMs, Integer speedSlotsPerTick) {
        if (tickIntervalMs == null && speedSlotsPerTick == null) {
            throw new IllegalArgumentException("At least one parameter must be provided for update.");
        }
        Belt belt = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Belt", id));
        Instant now = Instant.now();

        if (tickIntervalMs != null) {
            belt.setTickIntervalMs(tickIntervalMs, now);
        }
        if (speedSlotsPerTick != null) {
            belt.setSpeedSlotsPerTick(speedSlotsPerTick, now);
        }
        return repository.save(belt);
    }
}
