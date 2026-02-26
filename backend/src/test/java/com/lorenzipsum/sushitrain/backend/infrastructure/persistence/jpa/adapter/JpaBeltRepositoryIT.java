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

import static com.lorenzipsum.sushitrain.backend.domain.belt.Belt.TICK_INTERVAL_MS_DEFAULT_VALUE;
import static com.lorenzipsum.sushitrain.backend.testutil.TestFixtures.MAIN_BELT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        // important: adapt this test if you change data in script 'V3_seed_belt.sql'
        Belt belt = repository.findById(MAIN_BELT_ID).orElseThrow();
        assertThat(belt).as("belt").isNotNull();
        assertThat(belt.getName()).as("name").isEqualTo("Main Belt");
        assertThat(belt.getTickIntervalMs()).as("tickIntervalMs").isEqualTo(TICK_INTERVAL_MS_DEFAULT_VALUE);
        assertThat(belt.getSpeedSlotsPerTick()).as("speedSlotsPerTick").isEqualTo(1);
        assertThat(belt.getSlotCount()).as("slotCount").isEqualTo(192);
        assertThat(belt.getSlots()).as("slots").isNotNull().hasSize(192);
        assertThat(belt.getSeats()).as("seats").isNotNull().hasSize(24);
    }

    @Test
    @DisplayName("Persisting of new belt is complete and ok")
    void creatingAndLoadingBelt_ok() {
        var seatSpecs = List.of(new SeatSpec("A1", 1), new SeatSpec("B2", 7));
        var belt = Belt.create("Default", 10, seatSpecs);

        repository.create(belt);
        em.flush();
        em.clear();
        var loaded = repository.findById(belt.getId()).orElseThrow();

        assertThat(loaded.getId()).as("id").isEqualTo(belt.getId());
        assertThat(loaded.getName()).as("name").isEqualTo(belt.getName());
        assertThat(loaded.getSlotCount()).as("slotCount").isEqualTo(belt.getSlotCount());
        assertThat(loaded.getBaseRotationOffset()).as("baseRotationOffset").isEqualTo(belt.getBaseRotationOffset());
        assertThat(loaded.getTickIntervalMs()).as("tickIntervalMs").isEqualTo(belt.getTickIntervalMs());
        assertThat(loaded.getSpeedSlotsPerTick()).as("speedSlotsPerTick").isEqualTo(belt.getSpeedSlotsPerTick());

        assertThat(loaded.getSlots()).as("slots").isNotNull().hasSize(belt.getSlots().size());
        assertThat(loaded.getSlots().getFirst().getPositionIndex()).as("slots.first.positionIndex").isEqualTo(0);
        assertThat(loaded.getSlots().getLast().getPositionIndex()).as("slots.last.positionIndex").isEqualTo(9);
        assertThat(loaded.getSlots().getFirst().getPlateId()).as("slots.first.plateId").isNull();

        assertThat(loaded.getSeats()).as("seats").isNotNull().hasSize(2);

        var seatA1 = loaded.getSeats().stream().filter(s -> "A1".equals(s.getLabel())).findFirst().orElseThrow();
        var seatB2 = loaded.getSeats().stream().filter(s -> "B2".equals(s.getLabel())).findFirst().orElseThrow();

        assertThat(seatA1.getPositionIndex()).as("seatA1.positionIndex").isEqualTo(1);
        assertThat(seatB2.getPositionIndex()).as("seatB2.positionIndex").isEqualTo(7);
        assertThat(seatA1.getId()).as("seatA1.id").isNotNull();
        assertThat(seatB2.getId()).as("seatB2.id").isNotNull();
    }

    @Test
    @DisplayName("Updating parameters for existing belt ok without deleting slots")
    void updateExistingBelt_setTickIntervalMs_ok() {
        Belt defaultBelt = repository.findParamsById(MAIN_BELT_ID).orElseThrow();
        int snapshotBaseRotationOffset = defaultBelt.getBaseRotationOffset();
        Instant snapshotOffsetStartedAt = defaultBelt.getOffsetStartedAt();
        int snapshotTickIntervalMs = defaultBelt.getTickIntervalMs();
        int snapshotSpeedSlotsPerTick = defaultBelt.getSpeedSlotsPerTick();
        int snapshotSlotCount = defaultBelt.getSlotCount();

        defaultBelt.setTickIntervalMs(150, Instant.now());
        repository.saveParams(defaultBelt);
        em.flush();
        em.clear();
        Belt updatedBelt = repository.findParamsById(MAIN_BELT_ID).orElseThrow();

        assertThat(updatedBelt.getTickIntervalMs()).as("tickIntervalMs").isEqualTo(150);
        assertThat(updatedBelt.getSlotCount()).as("slotCount").isEqualTo(snapshotSlotCount);
        assertThat(updatedBelt.getSpeedSlotsPerTick()).as("speedSlotsPerTick").isEqualTo(snapshotSpeedSlotsPerTick);
        assertThat(updatedBelt.getBaseRotationOffset()).as("baseRotationOffset").isNotEqualTo(snapshotBaseRotationOffset);
        assertThat(updatedBelt.getOffsetStartedAt()).as("offsetStartedAt").isNotEqualTo(snapshotOffsetStartedAt);
        assertThat(updatedBelt.getTickIntervalMs()).as("tickIntervalMs (changed)").isNotEqualTo(snapshotTickIntervalMs);
    }

    @Test
    @DisplayName("persistAndLoadBelt_not_ok")
    void creatingBelt_not_ok() {
        assertThrows(IllegalArgumentException.class, () -> repository.create(null));
        assertThrows(IllegalArgumentException.class, () -> repository.findById(null));
    }
}
