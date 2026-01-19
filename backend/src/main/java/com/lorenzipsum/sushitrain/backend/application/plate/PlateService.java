package com.lorenzipsum.sushitrain.backend.application.plate;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.domain.plate.PlateRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class PlateService {

    private final PlateRepository repository;

    public PlateService(PlateRepository repository) {
        this.repository = repository;
    }

    public static Instant inTwoHours() {
        return Instant.now().plusSeconds(7200);
    }

    public Plate getPlate(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Plate.class.getName(), id));
    }

}
