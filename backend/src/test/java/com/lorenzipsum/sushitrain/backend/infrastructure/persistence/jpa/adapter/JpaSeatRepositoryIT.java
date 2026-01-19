package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.TestData;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.domain.seat.Seat;
import com.lorenzipsum.sushitrain.backend.domain.seat.SeatRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltSlotMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.SeatMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.SeatJpaDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = "com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa")
@EnableJpaRepositories(basePackageClasses = SeatJpaDao.class)
@Import({JpaSeatRepository.class, SeatMapper.class, JpaBeltRepository.class, BeltMapper.class, BeltSlotMapper.class})
class JpaSeatRepositoryIT extends JpaRepositoryBase {

    @Autowired
    private SeatRepository repository;
    @Autowired
    private BeltRepository beltRepository;

    @Test
    @DisplayName("persist and load a Seat via adapter")
    void persistAndLoadSeat_ok() {
        // Arrange
        var belt = TestData.newBelt();
        beltRepository.save(belt);
        var seat = Seat.create("A1", belt.getId(), 5);

        // Act
        var saved = repository.save(seat);
        em.flush();
        em.clear();
        var reloadedOpt = repository.findById(saved.getId());

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(reloadedOpt).isPresent();

        Seat reloaded = reloadedOpt.get();

        assertAll("Asserting reloaded values correct",
                () -> assertEquals(seat.getId(), reloaded.getId()),
                () -> assertEquals(seat.getLabel(), reloaded.getLabel()),
                () -> assertEquals(seat.getSeatPositionIndex(), reloaded.getSeatPositionIndex()),
                () -> assertEquals(seat.getBeltId(), reloaded.getBeltId())
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
