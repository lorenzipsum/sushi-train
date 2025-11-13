package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.TestData;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltJpaDao;
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

import static com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter.IntegrationTestData.createDb;
import static com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter.IntegrationTestData.registerDynamicProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = "com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa")
@EnableJpaRepositories(basePackageClasses = BeltJpaDao.class)
@Import({JpaBeltRepository.class, BeltMapper.class}) // <-- import adapter + mapper only
class JpaBeltRepositoryIT {

    @Container
    static final PostgreSQLContainer<?> DB = createDb();

    @DynamicPropertySource
    @SuppressWarnings("unused")
    static void registerProps(DynamicPropertyRegistry r) {
        registerDynamicProperties(r, DB);
    }

    @Autowired
    @SuppressWarnings("unused")
    private BeltRepository repository; // the hex port implemented by JpaMenuItemRepository

    @Test
    @DisplayName("persist and load a Belt via adapter")
    void persistAndLoadBelt_ok() {
        // Arrange
        var belt = TestData.defaultBelt();

        // Act
        var saved = repository.save(belt);
        var reloadedOpt = repository.findById(saved.getId());

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(reloadedOpt).isPresent();

        Belt reloaded = reloadedOpt.get();

        assertAll("Asserting reloaded values correct",
                () -> assertEquals(saved.getId(), reloaded.getId()),
                () -> assertEquals("Default", reloaded.getName()),
                () -> assertEquals(10, reloaded.getSlotCount()),
                () -> assertEquals(0, reloaded.getRotationOffset()),
                () -> assertEquals(1000, reloaded.getTickIntervalMs()),
                () -> assertEquals(1, reloaded.getSpeedSlotsPerTick()),
                () -> assertEquals(10, reloaded.getSlots().size())
        );
    }

    @Test
    @DisplayName("persist checks for null values")
    void persistAndLoadBelt_not_ok() {
        assertAll("Asserting null handling",
                () -> assertThrows(IllegalArgumentException.class, () -> repository.save(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findById(null))
        );
    }
}
