package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItem;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItemRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.MenuItemMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.MenuItemJpaDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.temporal.ChronoUnit;

import static com.lorenzipsum.sushitrain.backend.TestData.SALMON_NIGIRI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = "com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa")
@EnableJpaRepositories(basePackageClasses = MenuItemJpaDao.class)
@Import({JpaMenuItemRepository.class, MenuItemMapper.class})
class JpaMenuItemRepositoryIT extends JpaRepositoryBase {

    @Autowired
    private MenuItemRepository repository;

    @Test
    @DisplayName("persist and load a MenuItem via hex adapter")
    void persistAndLoadMenuItem() {
        // Arrange
        var menuItem = MenuItem.create(SALMON_NIGIRI, PlateTier.GREEN, new MoneyYen(120));

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
