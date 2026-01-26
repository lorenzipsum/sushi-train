package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.TestData;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.domain.plate.PlateRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltSlotMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.PlateMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static com.lorenzipsum.sushitrain.backend.TestData.MENU_ITEM_DEFAULT_ID;
import static org.junit.jupiter.api.Assertions.*;

@Import({JpaBeltRepository.class,
        JpaPlateRepository.class,
        BeltMapper.class,
        BeltSlotMapper.class,
        PlateMapper.class})
class JpaBeltITRepositoryIT extends JpaBaseRepositoryIT {

    @Autowired
    private BeltRepository repository;
    @Autowired
    private PlateRepository plateRepository;

    @Test
    @DisplayName("persist and load a Belt via adapter")
    void persistAndLoadBelt_ok() {
        // Arrange
        var belt = TestData.newBelt();
        var firstSlot = belt.getSlots().getFirst();

        // Act
        var saved = repository.save(belt);
        em.flush();
        em.clear();
        var loaded = repository.findById(saved.getId()).orElseThrow();
        var loadedFirstSlot = loaded.getSlots().getFirst();

        // Assert
        assertAll("Asserting reloaded values correct",
                () -> assertEquals(belt.getId(), loaded.getId()),
                () -> assertEquals(belt.getName(), loaded.getName()),
                () -> assertEquals(belt.getSlotCount(), loaded.getSlotCount()),
                () -> assertEquals(belt.getBaseRotationOffset(), loaded.getBaseRotationOffset()),
                () -> assertEquals(belt.getTickIntervalMs(), loaded.getTickIntervalMs()),
                () -> assertEquals(belt.getSpeedSlotsPerTick(), loaded.getSpeedSlotsPerTick()),
                () -> assertEquals(belt.getSlots().size(), loaded.getSlots().size()),
                () -> assertEquals(firstSlot.getId(), loadedFirstSlot.getId()),
                () -> assertEquals(firstSlot.getBeltId(), loadedFirstSlot.getBeltId()),
                () -> assertEquals(firstSlot.getPositionIndex(), loadedFirstSlot.getPositionIndex()),
                () -> assertNull(firstSlot.getPlateId())
        );
    }

    @Test
    @DisplayName("place plate on slot")
    void persistAndLoad_place_plate_on_slot_ok() {
        // Arrange
        var belt = TestData.newBelt();
        var plate = Plate.create(MENU_ITEM_DEFAULT_ID, PlateTier.GREEN, MoneyYen.of(400), TestData.inTwoHours());
        var firstSlot = belt.getSlots().getFirst();

        // Act
        firstSlot.place(plate.getId());
        plateRepository.save(plate);
        repository.save(belt);
        em.flush();
        em.clear();

        // Assert
        var loadedFirstSlot = repository.findById(belt.getId())
                .orElseThrow()
                .getSlots()
                .getFirst();

        assertAll("Asserting that Plate on BeltSlot was persisted correctly",
                () -> assertNotNull(loadedFirstSlot.getPlateId()),
                () -> assertEquals(plate.getId(), loadedFirstSlot.getPlateId())
        );
    }

    @Test
    @DisplayName("pick plate on slot ")
    void persistAndLoad_pick_plate_from_slot_ok() {
        // Arrange
        var belt = TestData.newBelt();
        var plate = Plate.create(MENU_ITEM_DEFAULT_ID, PlateTier.GREEN, MoneyYen.of(400), TestData.inTwoHours());
        var firstSlot = belt.getSlots().getFirst();

        firstSlot.place(plate.getId());
        plateRepository.save(plate);
        repository.save(belt);
        em.flush();
        em.clear();

        // Act
        var loaded = repository.findById(belt.getId()).orElseThrow();
        var loadedFirstSlot = loaded
                .getSlots()
                .stream()
                .filter((bs) -> bs.getId().equals(firstSlot.getId()))
                .findFirst()
                .orElseThrow();
        UUID takenPlate = loadedFirstSlot.take();
        repository.save(loaded);
        em.flush();
        em.clear();

        var reLoadedFirstSlot = repository.findById(belt.getId())
                .orElseThrow()
                .getSlots()
                .stream()
                .filter((bs) -> bs.getId().equals(firstSlot.getId()))
                .findFirst()
                .orElseThrow();

        // Assert
        assertEquals(plate.getId(), takenPlate);
        assertNull(reLoadedFirstSlot.getPlateId());
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
