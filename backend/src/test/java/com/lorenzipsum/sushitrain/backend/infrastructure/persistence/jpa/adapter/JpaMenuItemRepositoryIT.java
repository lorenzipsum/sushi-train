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

import static com.lorenzipsum.sushitrain.backend.testutil.TestFixtures.SALMON_NIGIRI_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Import({JpaMenuItemRepository.class, MenuItemMapper.class})
class JpaMenuItemRepositoryIT extends JpaBaseRepositoryIT {

    @Autowired
    private MenuItemRepository repository;

    @Test
    @DisplayName("load existing menu item fetches all data")
    void loadExistingMenuItem_ok() {
        // important: adapt this test if you change data in script 'V4_seed_menu_item.sql'
        MenuItem menuItem = repository.findById(SALMON_NIGIRI_ID).orElseThrow();
        assertThat(menuItem).as("menuItem").isNotNull();
        assertThat(menuItem.getName()).as("name").isEqualTo("Salmon Nigiri");
        assertThat(menuItem.getDefaultTier()).as("defaultTier").isEqualTo(PlateTier.GREEN);
        assertThat(menuItem.getBasePrice()).as("basePrice").isEqualTo(new MoneyYen(450));
        assertThat(menuItem.getCreatedAt()).as("createdAt").isNotNull();
    }

    @Test
    @DisplayName("persist and load a new MenuItem")
    void persistAndLoadNewMenuItem_ok() {
        var menuItem = MenuItem.create("New Menu Item", PlateTier.GREEN, new MoneyYen(120));
        var savedId = repository.save(menuItem).getId();
        em.flush();
        em.clear();
        var reloadedOpt = repository.findById(savedId);

        assertThat(reloadedOpt).as("reloadedOpt").isPresent();
        var reloaded = reloadedOpt.orElseThrow();

        assertThat(reloaded.getId()).as("id").isEqualTo(menuItem.getId());
        assertThat(reloaded.getName()).as("name").isEqualTo(menuItem.getName());
        assertThat(reloaded.getDefaultTier()).as("defaultTier").isEqualTo(menuItem.getDefaultTier());
        assertThat(reloaded.getBasePrice()).as("basePrice").isEqualTo(menuItem.getBasePrice());
        assertThat(reloaded.getCreatedAt()).as("createdAt").isNotNull();
        assertThat(reloaded.getCreatedAt().truncatedTo(ChronoUnit.MILLIS)).as("createdAt (truncatedToMillis)").isEqualTo(menuItem.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
    }

    @Test
    @DisplayName("repository rejects null arguments")
    void persistAndLoadMenuItem_not_ok() {
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
        assertThrows(IllegalArgumentException.class, () -> repository.findById(null));
    }
}
