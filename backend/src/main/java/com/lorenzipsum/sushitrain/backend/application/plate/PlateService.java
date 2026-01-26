package com.lorenzipsum.sushitrain.backend.application.plate;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItemRepository;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.domain.plate.PlateRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PlateService {

    private final PlateRepository repository;
    private final MenuItemRepository menuItemRepository;

    public PlateService(PlateRepository repository, MenuItemRepository menuItemRepository) {
        this.repository = repository;
        this.menuItemRepository = menuItemRepository;
    }

    private static Instant inTwoHours() {
        return Instant.now().plusSeconds(7200);
    }

    public Plate getPlate(UUID id) {
        return repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Plate", id));
    }

    public Plate createPlate(UUID menuItemId, PlateTier tierSnapshot, MoneyYen priceAtCreation, Instant expiresAt) {
        var menuItem = menuItemRepository.findById(menuItemId).orElseThrow(
                () -> new ResourceNotFoundException("MenuItem", menuItemId));

        var tier = (tierSnapshot != null) ? tierSnapshot : menuItem.getDefaultTier();
        var price = (priceAtCreation != null) ? priceAtCreation : menuItem.getBasePrice();
        var expires = (expiresAt != null) ? expiresAt : inTwoHours();

        var plate = Plate.create(menuItemId, tier, price, expires);

        return repository.save(plate);
    }

    public Plate expirePlate(UUID id) {
        var plate = getPlate(id);
        plate.expire();
        return repository.save(plate);
    }

    public List<Plate> getAllPlates() {
        return repository.findAll();
    }
}
