package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.TestData;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltSlot;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltSlotRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltSlotEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltSlotMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltSlotJpaDao;
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

import java.util.UUID;

import static com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter.IntegrationTestData.createDb;
import static com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter.IntegrationTestData.registerDynamicProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackageClasses = {BeltSlotEntity.class, BeltEntity.class})
@EnableJpaRepositories(basePackageClasses = {BeltSlotJpaDao.class, BeltJpaDao.class})
@Import({JpaBeltSlotRepository.class, BeltSlotMapper.class, JpaBeltRepository.class, BeltMapper.class}) // <-- import adapter + mapper only
class JpaBeltSlotRepositoryIT {

    @Container
    static final PostgreSQLContainer<?> DB = createDb();

    @DynamicPropertySource
    @SuppressWarnings("unused")
    static void registerProps(DynamicPropertyRegistry r) {
        registerDynamicProperties(r, DB);
    }

    @Autowired
    @SuppressWarnings("unused")
    private BeltSlotRepository repository; // the hex port implemented by JpaMenuItemRepository
    @Autowired
    @SuppressWarnings("unused")
    private BeltRepository beltRepository;

    @Test
    @DisplayName("persist and load a BeltSlot via adapter")
    void persistAndLoadBeltSlot() {
        // Arrange
        var belt = TestData.defaultBelt();
        var savedBelt = beltRepository.save(belt);
        var firstSlot = savedBelt.getSlots().getFirst();

        // Act
        var saved = repository.save(firstSlot);
        var reloadedOpt = repository.findById(saved.getId());

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(reloadedOpt).isPresent();

        BeltSlot reloaded = reloadedOpt.get();

        assertAll("Asserting reloaded values correct",
                () -> assertEquals(saved.getId(), reloaded.getId()),
                () -> assertEquals(saved.getBeltId(), reloaded.getBeltId()),
                () -> assertEquals(saved.getPositionIndex(), reloaded.getPositionIndex()),
                () -> assertEquals(saved.getPlateId(), reloaded.getPlateId())
        );
    }

    @Test
    @DisplayName("persist checks for null values")
    void persistAndLoadBelt_not_ok() {
        assertAll("Asserting null handling",
                () -> assertThrows(IllegalArgumentException.class, () -> repository.save(null)),
                () -> assertThrows(IllegalStateException.class, () -> repository.save(BeltSlot.createEmptyAt(UUID.randomUUID(), 1))),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findById(null))
        );
    }
}
