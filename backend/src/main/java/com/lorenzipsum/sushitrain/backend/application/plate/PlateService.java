package com.lorenzipsum.sushitrain.backend.application.plate;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItemRepository;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.domain.plate.PlateRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class PlateService {

    private final PlateRepository repository;
    private final MenuItemRepository menuItemRepository;

    public PlateService(PlateRepository repository, MenuItemRepository menuItemRepository) {
        this.repository = repository;
        this.menuItemRepository = menuItemRepository;
    }

    public static Instant inTwoHours() {
        return Instant.now().plusSeconds(7200);
    }

    public Plate getPlate(UUID id) {
        return repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(Plate.class.getName(), id));
    }

    public Plate createPlate(Plate plate) {
        var menuItemId = plate.getMenuItemId();
        var menuItem = menuItemRepository.findById(plate.getMenuItemId()).orElseThrow(
                () -> new ResourceNotFoundException("MenuItem", menuItemId));

        if (plate.getTierSnapshot() == null || plate.getPriceAtCreation() == null) {
            plate = Plate.create(
                    plate.getMenuItemId(),
                    menuItem.getDefaultTier(),
                    menuItem.getBasePrice(),
                    plate.getExpiresAt()
            );
        }

        if (plate.getExpiresAt() == null) {
            plate = Plate.create(
                    plate.getMenuItemId(),
                    plate.getTierSnapshot(),
                    plate.getPriceAtCreation(),
                    inTwoHours()
            );
        }

        return repository.save(plate);
    }
}
