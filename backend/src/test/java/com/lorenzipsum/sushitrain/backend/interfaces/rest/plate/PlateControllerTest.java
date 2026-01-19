package com.lorenzipsum.sushitrain.backend.interfaces.rest.plate;

import com.lorenzipsum.sushitrain.backend.application.plate.PlateService;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlateController.class)
class PlateControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PlateService service;
    @MockitoBean
    private PlateDtoMapper mapper;

    @Test
    void getPlate() throws Exception {
        // arrange
        UUID menuItemId = UUID.randomUUID();
        PlateTier tier = PlateTier.GREEN;
        MoneyYen price = MoneyYen.of(300);
        Instant expiresAt = Instant.now().plusSeconds(600);

        Plate plate = Plate.create(menuItemId, tier, price, expiresAt);
        UUID plateId = plate.getId();
        given(service.getPlate(plateId)).willReturn(plate);

        PlateDto plateDto = new PlateDto(
                plate.getId(),
                plate.getMenuItemId(),
                plate.getTierSnapshot(),
                plate.getPriceAtCreation().amount(),
                plate.getCreatedAt(),
                plate.getExpiresAt(),
                plate.getStatus()
        );
        given(mapper.toDto(plate)).willReturn(plateDto);

        // act & assert
        mockMvc.perform(get("/api/v1/plates/" + plate.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(plateId.toString()))
                .andExpect(jsonPath("$.menuItemId").value(menuItemId.toString()))
                .andExpect(jsonPath("$.tierSnapshot").value(tier.toString()))
                .andExpect(jsonPath("$.priceAtCreation").value(price.amount()))
                .andExpect(jsonPath("$.createdAt").value(plate.getCreatedAt().toString()))
                .andExpect(jsonPath("$.expiresAt").value(expiresAt.toString()))
                .andExpect(jsonPath("$.status").value(PlateStatus.ON_BELT.toString()));
    }
}