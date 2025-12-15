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
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.lorenzipsum.sushitrain.backend.domain.TestData.SALMON_NIGIRI;
import static com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter.IntegrationTestData.createDb;
import static com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter.IntegrationTestData.registerDynamicProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = "com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa")
@EnableJpaRepositories(basePackageClasses = MenuItemJpaDao.class)
@Import({JpaMenuItemRepository.class, MenuItemMapper.class}) // <-- import adapter + mapper only
class JpaMenuItemRepositoryIT {

    @Container
    static final PostgreSQLContainer DB = createDb();

    @DynamicPropertySource
    @SuppressWarnings("unused")
    static void registerProps(DynamicPropertyRegistry r) {
        registerDynamicProperties(r, DB);
    }

    @Autowired
    @SuppressWarnings("unused")
    private MenuItemRepository repository;

    @Test
    @DisplayName("persist and load a MenuItem via hex adapter")
    void persistAndLoadMenuItem() {
        // Arrange
        MenuItem salmon = MenuItem.create(SALMON_NIGIRI, PlateTier.GREEN, new MoneyYen(120));

        // Act
        MenuItem saved = repository.save(salmon);
        var reloadedOpt = repository.findById(saved.getId());

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(reloadedOpt).isPresent();

        MenuItem reloaded = reloadedOpt.get();
        assertThat(reloaded.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getName()).isEqualTo(SALMON_NIGIRI);
        assertThat(reloaded.getDefaultTier()).isEqualTo(PlateTier.GREEN);
        assertThat(reloaded.getBasePrice().amount()).isEqualTo(120);
        assertThat(reloaded.getCreatedAt()).isNotNull();
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
