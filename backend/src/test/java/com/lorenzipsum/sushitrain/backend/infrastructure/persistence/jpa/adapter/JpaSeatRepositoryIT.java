package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.domain.seat.Seat;
import com.lorenzipsum.sushitrain.backend.domain.seat.SeatRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltSlotMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.SeatMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Import({JpaSeatRepository.class, SeatMapper.class, JpaBeltRepositoryIT.class, BeltMapper.class, BeltSlotMapper.class})
class JpaSeatRepositoryIT extends JpaBaseRepositoryIT {

    @Autowired
    private SeatRepository repository;
    @Autowired
    private BeltRepository beltRepository;

    @Test
    @DisplayName("persist and load a Seat via adapter")
    void persistAndLoadSeat_ok() {
        // Arrange
        var belt = Belt.create("Default", 10);
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
                () -> assertEquals(seat.getPositionIndex(), reloaded.getPositionIndex()),
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
