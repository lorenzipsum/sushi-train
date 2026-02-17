package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItemRepository;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.domain.plate.PlateRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.MenuItemMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.PlateMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.temporal.ChronoUnit;

import static com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus.EXPIRED;
import static com.lorenzipsum.sushitrain.backend.testutil.TestFixtures.SALMON_NIGIRI_ID;
import static com.lorenzipsum.sushitrain.backend.testutil.TestFixtures.inTwoHours;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Import({JpaPlateRepository.class, PlateMapper.class, JpaMenuItemRepository.class, MenuItemMapper.class})
class JpaPlateRepositoryIT extends JpaBaseRepositoryIT {

    @Autowired
    private PlateRepository repository;
    @Autowired
    private MenuItemRepository menuItemRepository;

    @Test
    @DisplayName("persist and load a Plate via adapter")
    void persistAndLoadPlate() {
        var menuItem = menuItemRepository.findById(SALMON_NIGIRI_ID).orElseThrow();
        var plate = Plate.create(menuItem.getId(), menuItem.getDefaultTier(), menuItem.getBasePrice(), inTwoHours());

        var saved = repository.save(plate);
        em.flush();
        em.clear();
        var reloadedOpt = repository.findById(saved.getId());

        assertThat(saved.getId()).as("saved.id").isNotNull();
        assertThat(reloadedOpt).as("reloadedOpt").isPresent();

        var reloaded = reloadedOpt.orElseThrow();

        assertThat(reloaded.getId()).as("id").isEqualTo(plate.getId());
        assertThat(reloaded.getTierSnapshot()).as("tierSnapshot").isEqualTo(plate.getTierSnapshot());
        assertThat(reloaded.getPriceAtCreation()).as("priceAtCreation").isEqualTo(plate.getPriceAtCreation());
        assertThat(reloaded.getExpiresAt()).as("expiresAt").isNotNull();
        assertThat(reloaded.getExpiresAt().truncatedTo(ChronoUnit.MILLIS)).as("expiresAt (truncatedToMillis)").isEqualTo(plate.getExpiresAt().truncatedTo(ChronoUnit.MILLIS));
        assertThat(reloaded.getStatus()).as("status").isEqualTo(plate.getStatus());
        assertThat(reloaded.getCreatedAt()).as("createdAt").isNotNull();
        assertThat(reloaded.getCreatedAt().truncatedTo(ChronoUnit.MILLIS)).as("createdAt (truncatedToMillis)").isEqualTo(plate.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
        assertThat(reloaded.getMenuItemId()).as("menuItemId").isEqualTo(plate.getMenuItemId());
    }

    @Test
    @DisplayName("persist checks for null values")
    void persistAndLoadPlate_not_ok() {
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
        assertThrows(IllegalArgumentException.class, () -> repository.findById(null));
    }

    @Test
    @DisplayName("expire a plate and verify status change")
    void expirePlate_changesStatus() {
        var menuItem = menuItemRepository.findById(SALMON_NIGIRI_ID).orElseThrow();
        var plate = Plate.create(menuItem.getId(), menuItem.getDefaultTier(), menuItem.getBasePrice(), inTwoHours());
        var saved = repository.save(plate);
        em.flush();
        em.clear();

        var toExpire = repository.findById(saved.getId()).orElseThrow();
        toExpire.expire();

        var expired = repository.save(toExpire);
        em.flush();
        em.clear();
        var reloadedOpt = repository.findById(expired.getId());

        assertThat(reloadedOpt).as("reloadedOpt").isPresent();
        assertThat(reloadedOpt.orElseThrow().getStatus()).as("status").isEqualTo(EXPIRED);
    }
}