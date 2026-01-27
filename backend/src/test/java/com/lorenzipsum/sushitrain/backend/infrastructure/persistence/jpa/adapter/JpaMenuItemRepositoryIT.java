package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItem;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItemRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.MenuItemMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Import({JpaMenuItemRepository.class, MenuItemMapper.class})
class JpaMenuItemRepositoryIT extends JpaBaseRepositoryIT {

    @Autowired
    private MenuItemRepository repository;

    @Test
    @DisplayName("persist and load a MenuItem via hex adapter")
    void persistAndLoadMenuItem() {
        // Arrange
        var menuItem = MenuItem.create("New Menu Item", PlateTier.GREEN, new MoneyYen(120));

        // Act
        var savedId = repository.save(menuItem).getId();
        em.flush();
        em.clear();
        var reloadedOpt = repository.findById(savedId);

        // Assert
        assertThat(reloadedOpt).isPresent();
        MenuItem reloaded = reloadedOpt.get();

        assertAll("Asserting that menu was saved and reloaded successfully",
                () -> assertEquals(menuItem.getId(), reloaded.getId()),
                () -> assertEquals(menuItem.getName(), reloaded.getName()),
                () -> assertEquals(menuItem.getDefaultTier(), reloaded.getDefaultTier()),
                () -> assertEquals(menuItem.getBasePrice(), reloaded.getBasePrice()),
                () -> assertEquals(menuItem.getCreatedAt().truncatedTo(ChronoUnit.MILLIS), reloaded.getCreatedAt().truncatedTo(ChronoUnit.MILLIS)),
                () -> assertNotNull(reloaded.getCreatedAt())
        );
    }

    @Test
    @DisplayName("persist checks for null values")
    void persistAndLoadMenuItem_not_ok() {
        assertAll("Asserting null handling",
                () -> assertThrows(IllegalArgumentException.class, () -> repository.save(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findById(null))
        );
    }
}
