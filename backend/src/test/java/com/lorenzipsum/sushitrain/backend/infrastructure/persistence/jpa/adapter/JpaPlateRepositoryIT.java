package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.TestData;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItem;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItemRepository;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.domain.plate.PlateRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.MenuItemMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.PlateMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.MenuItemJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.PlateJpaDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.temporal.ChronoUnit;

import static com.lorenzipsum.sushitrain.backend.domain.TestData.SALMON_NIGIRI;
import static com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter.IntegrationTestData.createDb;
import static com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter.IntegrationTestData.registerDynamicProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = "com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa")
@EnableJpaRepositories(basePackageClasses = {PlateJpaDao.class, MenuItemJpaDao.class})
@Import({JpaPlateRepository.class, PlateMapper.class, JpaMenuItemRepository.class, MenuItemMapper.class})
class JpaPlateRepositoryIT {

    @Autowired
    TestEntityManager em;

    @Container
    static final PostgreSQLContainer DB = createDb();

    @DynamicPropertySource
    @SuppressWarnings("unused")
    static void registerProps(DynamicPropertyRegistry r) {
        registerDynamicProperties(r, DB);
    }

    @Autowired
    @SuppressWarnings("unused")
    private PlateRepository repository;

    @Autowired
    @SuppressWarnings("unused")
    private MenuItemRepository menuItemRepository;

    @Test
    @DisplayName("persist and load a Plate via adapter")
    void persistAndLoadPlate() {
        // Arrange
        var menuItem = menuItemRepository.save(MenuItem.create(SALMON_NIGIRI, PlateTier.GREEN, new MoneyYen(120)));
        var plate = Plate.create(menuItem.getId(), menuItem.getDefaultTier(), menuItem.getBasePrice(), TestData.inTwoHours());

        // Act
        var saved = repository.save(plate);
        em.flush();
        em.clear();
        var reloadedOpt = repository.findById(saved.getId());

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(reloadedOpt).isPresent();

        Plate reloaded = reloadedOpt.get();

        assertAll("Asserting reloaded values correct",
                () -> assertEquals(plate.getId(), reloaded.getId()),
                () -> assertEquals(plate.getTierSnapshot(), reloaded.getTierSnapshot()),
                () -> assertEquals(plate.getPriceAtCreation(), reloaded.getPriceAtCreation()),
                () -> assertEquals(plate.getExpiresAt().truncatedTo(ChronoUnit.MILLIS), reloaded.getExpiresAt().truncatedTo(ChronoUnit.MILLIS)),
                () -> assertEquals(plate.getStatus(), reloaded.getStatus()),
                () -> assertEquals(plate.getCreatedAt().truncatedTo(ChronoUnit.MILLIS), reloaded.getCreatedAt().truncatedTo(ChronoUnit.MILLIS)),
                () -> assertEquals(plate.getMenuItemId(), reloaded.getMenuItemId())
        );
    }

    @Test
    @DisplayName("persist checks for null values")
    void persistAndLoadPlate_not_ok() {
        assertAll("Asserting null handling",
                () -> assertThrows(IllegalArgumentException.class, () -> repository.save(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findById(null))
        );
    }
}
