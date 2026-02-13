package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt;

import com.lorenzipsum.sushitrain.backend.application.belt.BeltService;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.belt.SeatSpec;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto.BeltDtoMapperImpl;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto.BeltUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.lorenzipsum.sushitrain.backend.domain.belt.Belt.SPEED_SLOTS_PER_TICK_MAX_VALUE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeltController.class)
@Import({BeltDtoMapperImpl.class})
public class BeltControllerTest {
    private final String baseUrl = "/api/v1/belts";
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    BeltService beltService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testUpdateBeltParameters_both_params_ok() throws Exception {
        // arrange
        int newTickIntervalMs = 250;
        int newSpeedSlotsPerTick = 2;
        var now = Instant.now();
        var belt = Belt.create("Test Belt", 10, List.of(new SeatSpec("1", 3)));
        belt.setTickIntervalMs(newTickIntervalMs, now);
        belt.setSpeedSlotsPerTick(newSpeedSlotsPerTick, now);
        BeltUpdateRequest updateRequest = new BeltUpdateRequest(newTickIntervalMs, newSpeedSlotsPerTick);
        given(beltService.updateBeltParameters(any(), any(), any())).willReturn(belt);

        // act & assert
        mockMvc.perform(patch(baseUrl + "/{id}", belt.getId())
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.tickIntervalMs").value(newTickIntervalMs))
                .andExpect(jsonPath("$.speedSlotsPerTick").value(newSpeedSlotsPerTick));
    }


    @Test
    public void testUpdateBeltParameters_tickIntervalMs_ok() throws Exception {
        // arrange
        int newTickIntervalMs = 250;
        var now = Instant.now();
        var belt = Belt.create("Test Belt", 10, List.of(new SeatSpec("1", 3)));
        belt.setTickIntervalMs(newTickIntervalMs, now);
        BeltUpdateRequest updateRequest = new BeltUpdateRequest(newTickIntervalMs, null);
        given(beltService.updateBeltParameters(any(), any(), any())).willReturn(belt);

        // act & assert
        mockMvc.perform(patch(baseUrl + "/{id}", belt.getId())
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.tickIntervalMs").value(newTickIntervalMs));
    }


    @Test
    public void testUpdateBeltParameters_speedSlotsPerTick_ok() throws Exception {
        // arrange
        int newSpeedSlotsPerTick = 2;
        var now = Instant.now();
        var belt = Belt.create("Test Belt", 10, List.of(new SeatSpec("1", 3)));
        belt.setSpeedSlotsPerTick(newSpeedSlotsPerTick, now);
        BeltUpdateRequest updateRequest = new BeltUpdateRequest(null, newSpeedSlotsPerTick);
        given(beltService.updateBeltParameters(any(), any(), any())).willReturn(belt);

        // act & assert
        mockMvc.perform(patch(baseUrl + "/{id}", belt.getId())
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.speedSlotsPerTick").value(newSpeedSlotsPerTick));
    }

    @Test
    public void testUpdateBeltParameters_invalid_speedSlotsPerTick() throws Exception {
        // arrange
        var beltId = UUID.randomUUID();
        @SuppressWarnings("ConstantConditions")
        BeltUpdateRequest updateRequest = new BeltUpdateRequest(null, SPEED_SLOTS_PER_TICK_MAX_VALUE + 1);

        // act & assert
        mockMvc.perform(patch(baseUrl + "/{id}", beltId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://api.sushitrain/errors/validation-failed"));
    }

    @Test
    public void testUpdateBeltParameters_no_params() throws Exception {
        // arrange
        var beltId = UUID.randomUUID();
        BeltUpdateRequest updateRequest = new BeltUpdateRequest(null, null);

        // act & assert
        mockMvc.perform(patch(baseUrl + "/{id}", beltId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://api.sushitrain/errors/validation-failed"));
    }
}
