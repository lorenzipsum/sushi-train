package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto;

import com.lorenzipsum.sushitrain.backend.application.view.BeltSlotPlateView;
import com.lorenzipsum.sushitrain.backend.domain.common.YenAmount;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BeltSnapshotDtoMapperTest {

    private final BeltApiMapper mapper = new BeltApiMapperImpl();

    @Test
    void mapsRowsToSnapshotDto_ok() {
        var beltId = UUID.randomUUID();
        var slot0 = UUID.randomUUID();
        var slot1 = UUID.randomUUID();
        var plateId = UUID.randomUUID();
        var menuItemId = UUID.randomUUID();
        var now = Instant.parse("2026-02-20T00:00:00Z");
        var expires = Instant.parse("2026-02-20T02:00:00Z");

        var rows = List.of(
                new BeltSlotPlateView(beltId, "Main Belt", 2, 10, now, 900, 3, slot0, 0, null, null, null, null, null, null, null),
                new BeltSlotPlateView(beltId, "Main Belt", 2, 10, now, 900, 3, slot1, 1, plateId, menuItemId, "Salmon Nigiri", PlateTier.GREEN, YenAmount.of(450), PlateStatus.ON_BELT, expires)
        );

        var dto = mapper.toSnapshotDto(rows);

        assertThat(dto).isNotNull();
        assertThat(dto.beltId()).isEqualTo(beltId);
        assertThat(dto.slots()).hasSize(2);

        assertThat(dto.slots().get(0).positionIndex()).isEqualTo(0);
        assertThat(dto.slots().get(0).plate()).isNull();

        assertThat(dto.slots().get(1).positionIndex()).isEqualTo(1);
        assertThat(dto.slots().get(1).plate()).isNotNull();
        assertThat(dto.slots().get(1).plate().menuItemName()).isEqualTo("Salmon Nigiri");
    }
}
