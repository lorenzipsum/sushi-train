package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.TestData;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItem;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItemRepository;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.domain.plate.PlateRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.MenuItemEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.PlateEntity;
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
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.lorenzipsum.sushitrain.backend.domain.TestData.SALMON_NIGIRI;
import static com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter.IntegrationTestData.createDb;
import static com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter.IntegrationTestData.registerDynamicProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackageClasses = {PlateEntity.class, MenuItemEntity.class})
@EnableJpaRepositories(basePackageClasses = {PlateJpaDao.class, MenuItemJpaDao.class})
@Import({JpaPlateRepository.class, PlateMapper.class, JpaMenuItemRepository.class, MenuItemMapper.class}) // <-- import adapter + mapper only
class JpaPlateRepositoryIT {

    @Container
    static final PostgreSQLContainer<?> DB = createDb();

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
        var reloadedOpt = repository.findById(saved.getId());

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(reloadedOpt).isPresent();

        Plate reloaded = reloadedOpt.get();

        assertAll("Asserting reloaded values correct",
                () -> assertEquals(saved.getId(), reloaded.getId()),
                () -> assertEquals(saved.getTierSnapshot(), reloaded.getTierSnapshot()),
                () -> assertEquals(saved.getPriceAtCreation(), reloaded.getPriceAtCreation()),
                () -> assertEquals(saved.getExpiresAt(), reloaded.getExpiresAt()),
                () -> assertEquals(saved.getStatus(), reloaded.getStatus()),
                () -> assertEquals(saved.getCreatedAt(), reloaded.getCreatedAt()),
                () -> assertEquals(saved.getMenuItemId(), reloaded.getMenuItemId())
        );
    }
}
