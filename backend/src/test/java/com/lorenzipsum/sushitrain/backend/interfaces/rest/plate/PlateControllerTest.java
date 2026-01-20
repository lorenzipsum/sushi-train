package com.lorenzipsum.sushitrain.backend.interfaces.rest.plate;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlateController.class)
class PlateControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PlateService service;
    @MockitoBean
    private PlateDtoMapper mapper;

    @Test
    void getPlate_returns200() throws Exception {
        // arrange
        UUID menuItemId = UUID.randomUUID();
        PlateTier tier = PlateTier.GREEN;
        MoneyYen price = MoneyYen.of(300);
        Instant expiresAt = Instant.now().plusSeconds(600);

        Plate plate = Plate.create(menuItemId, tier, price, expiresAt);
        UUID plateId = plate.getId();

        PlateDto plateDto = new PlateDto(
                plate.getId(),
                plate.getMenuItemId(),
                plate.getTierSnapshot(),
                plate.getPriceAtCreation().amount(),
                plate.getCreatedAt(),
                plate.getExpiresAt(),
                plate.getStatus()
        );

        given(service.getPlate(plateId)).willReturn(plate);
        given(mapper.toDto(plate)).willReturn(plateDto);

        // act & assert
        mockMvc.perform(get("/api/v1/plates/" + plateId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(plateId.toString()))
                .andExpect(jsonPath("$.menuItemId").value(menuItemId.toString()))
                .andExpect(jsonPath("$.tierSnapshot").value(tier.toString()))
                .andExpect(jsonPath("$.priceAtCreation").value(price.amount()))
                .andExpect(jsonPath("$.createdAt").value(plate.getCreatedAt().toString()))
                .andExpect(jsonPath("$.expiresAt").value(expiresAt.toString()))
                .andExpect(jsonPath("$.status").value(PlateStatus.ON_BELT.toString()));
    }

    @Test
    void getPlate_returns404_problemDetail() throws Exception {
        UUID id = UUID.randomUUID();
        given(service.getPlate(id)).willThrow(new ResourceNotFoundException("Plate", id));

        mockMvc.perform(get("/api/v1/plates/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Plate not found: " + id))
                .andExpect(jsonPath("$.instance").value("/api/v1/plates/" + id))
                .andExpect(jsonPath("$.type").value("https://api.sushitrain/errors/not-found"));
    }

    @Test
    void getPlate_returns400_problemDetail_onInvalidUuid() throws Exception {
        mockMvc.perform(get("/api/v1/plates/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Invalid parameter"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.instance").value("/api/v1/plates/not-a-uuid"))
                .andExpect(jsonPath("$.type").value("https://api.sushitrain/errors/invalid-parameter"));
    }
}