package com.lorenzipsum.sushitrain.backend.interfaces.rest.plate;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.application.plate.PlateService;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.plate.dto.CreatePlateRequest;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.plate.dto.PlateDto;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.plate.dto.PlateDtoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlateController.class)
class PlateControllerTest {
    public static final String BASE_URI = "/api/v1/plates";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PlateService service;
    @MockitoBean
    private PlateDtoMapper mapper;
    @Autowired
    private ObjectMapper objectMapper;

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
        mockMvc.perform(get(BASE_URI + "/" + plateId))
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

        mockMvc.perform(get(BASE_URI + "/{id}", id))
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
        mockMvc.perform(get(BASE_URI + "/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Invalid parameter"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.instance").value("/api/v1/plates/not-a-uuid"))
                .andExpect(jsonPath("$.type").value("https://api.sushitrain/errors/invalid-parameter"));
    }

    @Test
    void createPlate_returns201() throws Exception {
        // arrange
        var requestDto = new CreatePlateRequest(UUID.randomUUID(), PlateTier.RED, 500, Instant.now().plusSeconds(3600));
        var plate = Plate.create(requestDto.menuItemId(), requestDto.tierSnapshot(), MoneyYen.of(requestDto.priceAtCreation()), requestDto.expiresAt());
        var responseDto = new PlateDto(
                plate.getId(),
                plate.getMenuItemId(),
                plate.getTierSnapshot(),
                plate.getPriceAtCreation().amount(),
                plate.getCreatedAt(),
                plate.getExpiresAt(),
                plate.getStatus()
        );
        given(mapper.toDomain(requestDto)).willReturn(plate);
        given(service.createPlate(plate)).willReturn(plate);
        given(mapper.toDto(plate)).willReturn(responseDto);

        // act & assert
        mockMvc.perform(post(BASE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.menuItemId").value(requestDto.menuItemId().toString()))
                .andExpect(jsonPath("$.tierSnapshot").value(requestDto.tierSnapshot().toString()))
                .andExpect(jsonPath("$.priceAtCreation").value(requestDto.priceAtCreation().toString()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.expiresAt").value(requestDto.expiresAt().toString()))
                .andExpect(jsonPath("$.status").value(PlateStatus.ON_BELT.toString()));
    }


    @Test
    void createPlate_returns400_problemDetail_onValidationError() throws Exception {
        // invalid: missing menuItemId, expiresAt
        var invalidJson = """
                {"tierSnapshot":"RED","priceAtCreation":500}
                """;

        mockMvc.perform(post(BASE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://api.sushitrain/errors/validation-failed"));
    }

    @Test
    void createPlate_returns500_problemDetail_onUnexpected() throws Exception {
        var request = new CreatePlateRequest(
                UUID.randomUUID(),
                PlateTier.RED,
                500,
                Instant.now().plusSeconds(3600)
        );

        given(mapper.toDomain(request)).willThrow(new RuntimeException("boom"));

        mockMvc.perform(post(BASE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Internal server error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.type").value("https://api.sushitrain/errors/internal"));
    }
}