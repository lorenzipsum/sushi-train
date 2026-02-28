package com.lorenzipsum.sushitrain.backend.application.plate;

import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.common.YenAmount;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItemRepository;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.domain.plate.PlateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlateServiceTest {

    @Mock
    private PlateRepository plateRepository;
    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private PlateBeltCommandPort plateBeltCommandPort;

    @Test
    void expirePlate_clearsBeltAssignment() {
        UUID plateId = UUID.randomUUID();
        Plate plate = Plate.rehydrate(
                plateId,
                UUID.randomUUID(),
                PlateTier.GREEN,
                YenAmount.of(300),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T02:00:00Z"),
                PlateStatus.ON_BELT
        );

        when(plateRepository.findById(plateId)).thenReturn(Optional.of(plate));
        when(plateRepository.save(plate)).thenReturn(plate);

        PlateService service = new PlateService(plateRepository, menuItemRepository, plateBeltCommandPort);

        service.expirePlate(plateId);

        verify(plateRepository).save(plate);
        verify(plateBeltCommandPort).clearPlateAssignment(plateId);
    }
}
