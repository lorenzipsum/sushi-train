package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.domain.belt.SeatSpec;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltSlotMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.PlateMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.SeatMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static com.lorenzipsum.sushitrain.backend.domain.belt.Belt.DEFAULT_TICK_INTERVAL_MS;
import static com.lorenzipsum.sushitrain.backend.testutil.TestFixtures.MAIN_BELT_ID;
import static org.junit.jupiter.api.Assertions.*;

@Import({
        JpaBeltRepository.class,
        JpaPlateRepository.class,
        BeltMapper.class,
        BeltSlotMapper.class,
        SeatMapper.class,
        PlateMapper.class
})
class JpaBeltRepositoryIT extends JpaBaseRepositoryIT {

    @Autowired
    private BeltRepository repository;

    @Test
    @DisplayName("Loading existing default belt fetches all data")
    void loadExistingBelt_ok() {
        // important: make sure to adapt this test in case you change data in script 'V3_seed_belt.sql'
        Belt belt = repository.findById(MAIN_BELT_ID).orElseThrow();

        assertAll("Check default belt data",
                () -> assertEquals("Main Belt", belt.getName()),
                () -> assertEquals(DEFAULT_TICK_INTERVAL_MS, belt.getTickIntervalMs()),
                () -> assertEquals(1, belt.getSpeedSlotsPerTick()),
                () -> assertEquals(192, belt.getSlotCount()),
                () -> assertEquals(192, belt.getSlots().size()),
                () -> assertEquals(24, belt.getSeats().size())
        );
    }

    @Test
    @DisplayName("Persisting of new belt is complete and ok")
    void persistAndLoadNewBelt_includesSlotsAndSeats_ok() {
        // Arrange
        var seatSpecs = List.of(
                new SeatSpec("A1", 1),
                new SeatSpec("B2", 7)
        );
        var belt = Belt.create("Default", 10, seatSpecs);

        // Act
        repository.save(belt);
        em.flush();
        em.clear();

        var loaded = repository.findById(belt.getId()).orElseThrow();

        // Assert (belt)
        assertAll("Asserting belt basics",
                () -> assertEquals(belt.getId(), loaded.getId()),
                () -> assertEquals(belt.getName(), loaded.getName()),
                () -> assertEquals(belt.getSlotCount(), loaded.getSlotCount()),
                () -> assertEquals(belt.getBaseRotationOffset(), loaded.getBaseRotationOffset()),
                () -> assertEquals(belt.getTickIntervalMs(), loaded.getTickIntervalMs()),
                () -> assertEquals(belt.getSpeedSlotsPerTick(), loaded.getSpeedSlotsPerTick())
        );

        // Assert (slots)
        assertAll("Asserting slots persisted",
                () -> assertNotNull(loaded.getSlots()),
                () -> assertEquals(belt.getSlots().size(), loaded.getSlots().size()),
                () -> assertEquals(0, loaded.getSlots().getFirst().getPositionIndex()),
                () -> assertEquals(9, loaded.getSlots().getLast().getPositionIndex()),
                () -> assertNull(loaded.getSlots().getFirst().getPlateId())
        );

        // Assert (seats)
        assertAll("Asserting seats persisted",
                () -> assertNotNull(loaded.getSeats()),
                () -> assertEquals(2, loaded.getSeats().size())
        );

        var seatA1 = loaded.getSeats().stream()
                .filter(s -> "A1".equals(s.getLabel()))
                .findFirst()
                .orElseThrow();

        var seatB2 = loaded.getSeats().stream()
                .filter(s -> "B2".equals(s.getLabel()))
                .findFirst()
                .orElseThrow();

        assertAll("Asserting seat details",
                () -> assertEquals(1, seatA1.getPositionIndex()),
                () -> assertEquals(7, seatB2.getPositionIndex()),
                () -> assertNotNull(seatA1.getId()),
                () -> assertNotNull(seatB2.getId())
        );
    }

    @Test
    @DisplayName("Updating parameters for existing belt ok without deleting slots")
    void updateExistingBelt_setTickIntervalMs_ok() {
        // arrange
        Belt defaultBelt = repository.findById(MAIN_BELT_ID).orElseThrow();
        int snapshotBaseRotationOffset = defaultBelt.getBaseRotationOffset();
        Instant snapshotOffsetStartedAt = defaultBelt.getOffsetStartedAt();
        int snapshotTickIntervalMs = defaultBelt.getTickIntervalMs();
        int snapshotSpeedSlotsPerTick = defaultBelt.getSpeedSlotsPerTick();
        int snapshotSlotCount = defaultBelt.getSlotCount();

        // act
        defaultBelt.setTickIntervalMs(10, Instant.now());
        repository.save(defaultBelt);
        em.flush();
        Belt updatedBelt = repository.findById(MAIN_BELT_ID).orElseThrow();

        // assert
        assertAll("Check if setting tick interval updated the right fields",
                () -> assertEquals(10, updatedBelt.getTickIntervalMs()),
                () -> assertEquals(snapshotSlotCount, updatedBelt.getSlots().size()),
                () -> assertEquals(snapshotSpeedSlotsPerTick, updatedBelt.getSpeedSlotsPerTick()),
                () -> assertNotEquals(snapshotBaseRotationOffset, updatedBelt.getBaseRotationOffset()),
                () -> assertNotEquals(snapshotOffsetStartedAt, updatedBelt.getOffsetStartedAt()),
                () -> assertNotEquals(snapshotTickIntervalMs, updatedBelt.getTickIntervalMs())
        );
    }

    @Test
    @DisplayName("persistAndLoadBelt_not_ok")
    void persistAndLoadBelt_not_ok() {
        assertAll("Asserting null handling",
                () -> assertThrows(IllegalArgumentException.class, () -> repository.save(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findById(null))
        );
    }
}
