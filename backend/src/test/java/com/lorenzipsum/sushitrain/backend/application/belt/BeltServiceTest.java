package com.lorenzipsum.sushitrain.backend.application.belt;

import com.lorenzipsum.sushitrain.backend.application.plate.PlateService;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.common.YenAmount;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeltServiceTest {

    @Mock
    private BeltRepository repository;
    @Mock
    private BeltQueryPort beltQueryPort;
    @Mock
    private BeltSlotAllocationCommandPort beltSlotAllocationPort;
    @Mock
    private PlateService plateService;
    @Mock
    private BeltPlacementRules beltPlacementRules;

    @Test
    void createPlatesAndPlaceOnBelt_allowsNullPriceAtCreation() {
        UUID beltId = UUID.randomUUID();
        UUID menuItemId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();
        UUID plateId = UUID.randomUUID();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        Instant expiresAt = Instant.parse("2026-01-01T02:00:00Z");

        var belt = Belt.create("Main Belt", 10, List.of());
        var freeSlot = new BeltSlotAllocationCommandPort.FreeBeltSlot(slotId, 0);
        var plate = Plate.rehydrate(
                plateId,
                menuItemId,
                PlateTier.GREEN,
                YenAmount.of(450),
                now,
                expiresAt,
                PlateStatus.CREATED
        );

        when(repository.findParamsById(beltId)).thenReturn(Optional.of(belt));
        when(beltSlotAllocationPort.findFreeSlotsForUpdate(beltId)).thenReturn(List.of(freeSlot));
        when(beltPlacementRules.minEmptySlotsBetweenNewPlates()).thenReturn(1);
        when(plateService.createPlate(eq(menuItemId), isNull(), isNull(), isNull())).thenReturn(plate);
        when(plateService.save(plate)).thenReturn(plate);

        var service = new BeltService(repository, beltQueryPort, beltSlotAllocationPort, plateService, beltPlacementRules);
        var command = new CreatePlatesCommand(menuItemId, 1, null, null, null);

        var result = service.createPlatesAndPlaceOnBelt(beltId, command);

        assertThat(result.createdCount()).isEqualTo(1);
        assertThat(result.placedPlates()).hasSize(1);
        verify(plateService).createPlate(eq(menuItemId), isNull(), isNull(), isNull());
        verify(plateService).save(plate);
        verify(beltSlotAllocationPort).assignPlateToSlot(slotId, plateId);
    }
}
