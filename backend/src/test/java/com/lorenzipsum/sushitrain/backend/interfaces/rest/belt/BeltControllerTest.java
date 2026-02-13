package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt;

import com.lorenzipsum.sushitrain.backend.application.belt.BeltService;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.belt.SeatSpec;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto.BeltDtoMapperImpl;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto.BeltUpdateRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.lorenzipsum.sushitrain.backend.domain.belt.Belt.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeltController.class)
@Import({BeltDtoMapperImpl.class})
public class BeltControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    BeltService beltService;
    @Autowired
    private ObjectMapper objectMapper;

    static Stream<Arguments> validUpdates() {
        return Stream.of(
                Arguments.of(new BeltUpdateRequest(250, null), 250, null),
                Arguments.of(new BeltUpdateRequest(null, 2), null, 2),
                Arguments.of(new BeltUpdateRequest(250, 2), 250, 2)
        );
    }

    @SuppressWarnings("ConstantConditions")
    static Stream<Arguments> invalidUpdates() {
        return Stream.of(
                Arguments.of(new BeltUpdateRequest(null, null)),
                Arguments.of(new BeltUpdateRequest(TICK_INTERVAL_MS_MIN_VALUE - 1, null)),
                Arguments.of(new BeltUpdateRequest(TICK_INTERVAL_MS_MAX_VALUE + 1, null)),
                Arguments.of(new BeltUpdateRequest(null, SPEED_SLOTS_PER_TICK_MAX_VALUE + 1)),
                Arguments.of(new BeltUpdateRequest(null, SPEED_SLOTS_PER_TICK_MIN_VALUE - 1))
        );
    }

    @ParameterizedTest
    @MethodSource("validUpdates")
    public void testUpdateBeltParameters_valid_updates_ok(
            BeltUpdateRequest updateRequest,
            Integer expectedTicks,
            Integer expectedSpeed) throws Exception {
        // arrange
        var belt = newTestBelt();
        var now = Instant.now();
        if (expectedTicks != null) {
            belt.setTickIntervalMs(expectedTicks, now);
        }
        if (expectedSpeed != null) {
            belt.setSpeedSlotsPerTick(expectedSpeed, now);
        }
        given(beltService.updateBeltParameters(belt.getId(), expectedTicks, expectedSpeed)).willReturn(belt);

        // act & assert
        ResultActions result = patchUpdate(belt.getId(), updateRequest)
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE));
        if (expectedTicks != null) result.andExpect(jsonPath("$.tickIntervalMs").value(expectedTicks));
        if (expectedSpeed != null) result.andExpect(jsonPath("$.speedSlotsPerTick").value(expectedSpeed));

        verify(beltService).updateBeltParameters(belt.getId(), expectedTicks, expectedSpeed);
        verifyNoMoreInteractions(beltService);
    }

    @ParameterizedTest
    @MethodSource("invalidUpdates")
    public void testUpdateBeltParameters_invalid_updates_not_ok(BeltUpdateRequest updateRequest) throws Exception {
        // arrange
        var beltId = UUID.randomUUID();

        // act & assert
        patchUpdate(beltId, updateRequest)
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://api.sushitrain/errors/validation-failed"));
    }


    // helpers
    private Belt newTestBelt() {
        return Belt.create("Test Belt", 10, List.of(new SeatSpec("1", 3)));
    }

    private ResultActions patchUpdate(UUID beltId, BeltUpdateRequest req) throws Exception {
        String baseUrl = "/api/v1/belts";
        return mockMvc.perform(patch(baseUrl + "/{id}", beltId)
                .contentType(APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(req)));
    }
}
