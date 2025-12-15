package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.TestData;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.domain.seat.Seat;
import com.lorenzipsum.sushitrain.backend.domain.seat.SeatRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.SeatMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.SeatJpaDao;
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

import static com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter.IntegrationTestData.createDb;
import static com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter.IntegrationTestData.registerDynamicProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = "com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa")
@EnableJpaRepositories(basePackageClasses = SeatJpaDao.class)
@Import({JpaSeatRepository.class, SeatMapper.class, JpaBeltRepository.class, BeltMapper.class}) // <-- import adapter + mapper only
class JpaSeatRepositoryIT {

    @Container
    static final PostgreSQLContainer DB = createDb();

    @DynamicPropertySource
    @SuppressWarnings("unused")
    static void registerProps(DynamicPropertyRegistry r) {
        registerDynamicProperties(r, DB);
    }

    @Autowired
    @SuppressWarnings("unused")
    private SeatRepository repository; // the hex port implemented by JpaMenuItemRepository
    @Autowired
    @SuppressWarnings("unused")
    private BeltRepository beltRepository;

    @Test
    @DisplayName("persist and load a Seat via adapter")
    void persistAndLoadSeat_ok() {
        // Arrange
        var belt = TestData.defaultBelt();
        beltRepository.save(belt);
        var seat = Seat.create("A1", belt.getId(), 5);

        // Act
        var saved = repository.save(seat);
        var reloadedOpt = repository.findById(saved.getId());

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(reloadedOpt).isPresent();

        Seat reloaded = reloadedOpt.get();

        assertAll("Asserting reloaded values correct",
                () -> assertEquals(saved.getId(), reloaded.getId()),
                () -> assertEquals(saved.getLabel(), reloaded.getLabel()),
                () -> assertEquals(saved.getSeatPositionIndex(), reloaded.getSeatPositionIndex()),
                () -> assertEquals(saved.getBeltId(), reloaded.getBeltId())
        );
    }

    @Test
    @DisplayName("persist checks for null values")
    void persistAndLoadSeat_not_ok() {
        assertAll("Asserting null handling",
                () -> assertThrows(IllegalArgumentException.class, () -> repository.save(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findById(null))
        );
    }
}
