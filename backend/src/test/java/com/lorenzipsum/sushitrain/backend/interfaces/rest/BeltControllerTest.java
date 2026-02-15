package com.lorenzipsum.sushitrain.backend.interfaces.rest;

import com.lorenzipsum.sushitrain.backend.application.belt.BeltService;
import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.belt.SeatSpec;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.BeltController;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto.BeltDtoMapperImpl;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto.BeltUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.lorenzipsum.sushitrain.backend.domain.belt.Belt.*;
import static com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.BeltController.BASE_URL_BELT_CONTROLLER;
import static com.lorenzipsum.sushitrain.backend.interfaces.rest.common.ControllerAdvice.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Import(BeltDtoMapperImpl.class)
@WebMvcTest(BeltController.class)
@AutoConfigureRestTestClient
class BeltControllerTest {

    @Autowired
    RestTestClient client;

    @MockitoBean
    BeltService beltService;

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
                Arguments.of(new BeltUpdateRequest(null, null), "anyParamProvided"),
                Arguments.of(new BeltUpdateRequest(TICK_INTERVAL_MS_MIN_VALUE - 1, null), "tickIntervalMs"),
                Arguments.of(new BeltUpdateRequest(TICK_INTERVAL_MS_MAX_VALUE + 1, null), "tickIntervalMs"),
                Arguments.of(new BeltUpdateRequest(null, SPEED_SLOTS_PER_TICK_MAX_VALUE + 1), "speedSlotsPerTick"),
                Arguments.of(new BeltUpdateRequest(null, SPEED_SLOTS_PER_TICK_MIN_VALUE - 1), "speedSlotsPerTick")
        );
    }

    @ParameterizedTest
    @MethodSource("validUpdates")
    @DisplayName("PATCH /api/v1/belts/{id} with valid parameters should update belt and return 200")
    void updateBeltParameters_valid_updates_ok(
            BeltUpdateRequest updateRequest,
            Integer expectedTicks,
            Integer expectedSpeed
    ) {
        // arrange
        var belt = Belt.create("Test Belt", 10, List.of(new SeatSpec("1", 3)));
        var now = Instant.now();
        if (expectedTicks != null) belt.setTickIntervalMs(expectedTicks, now);
        if (expectedSpeed != null) belt.setSpeedSlotsPerTick(expectedSpeed, now);

        given(beltService.updateBeltParameters(belt.getId(), expectedTicks, expectedSpeed)).willReturn(belt);

        // act
        var body = client.patch()
                .uri(BASE_URL_BELT_CONTROLLER + "/{id}", belt.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateRequest)
                .exchange()
                // assert
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(belt.getId().toString());

        if (expectedTicks != null) {
            body.jsonPath("$.tickIntervalMs").isEqualTo(expectedTicks);
        }
        if (expectedSpeed != null) {
            body.jsonPath("$.speedSlotsPerTick").isEqualTo(expectedSpeed);
        }

        verify(beltService).updateBeltParameters(belt.getId(), expectedTicks, expectedSpeed);
        verifyNoMoreInteractions(beltService);
    }

    @ParameterizedTest
    @MethodSource("invalidUpdates")
    @DisplayName("PATCH /api/v1/belts/{id} with invalid parameters should return 400 with ProblemDetail")
    void updateBeltParameters_invalid_updates_not_ok(BeltUpdateRequest updateRequest, String errorKey) {
        // arrange
        var beltId = UUID.randomUUID();

        // act
        client.patch()
                .uri(BASE_URL_BELT_CONTROLLER + "/{id}", beltId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateRequest)
                .exchange()
                // assert
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_400_VALIDATION_FAILED_TITLE)
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.type").isEqualTo(PROBLEM_400_VALIDATION_FAILED_URI)
                .jsonPath("$.detail").isEqualTo("One or more fields are invalid")
                .jsonPath("$.instance").isEqualTo(BASE_URL_BELT_CONTROLLER + "/" + beltId)
                .jsonPath("$.errors").isMap()
                .jsonPath("$.errors." + errorKey).exists();

        verifyNoInteractions(beltService);
    }

    @Test
    @DisplayName("PATCH /api/v1/belts/{id} with non-UUID id should return 400 invalid-parameter ProblemDetail")
    void updateBeltParameters_invalid_uuid_returns_400_problemDetail() {
        // arrange
        var invalidId = "not-a-uuid";
        var request = new BeltUpdateRequest(250, null);

        // act + assert
        client.patch()
                .uri(BASE_URL_BELT_CONTROLLER + "/{id}", invalidId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_400_INVALID_PARAM_TITLE)
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.type").isEqualTo(PROBLEM_400_INVALID_PARAM_URI)
                .jsonPath("$.detail").isEqualTo("Parameter 'id' must be a UUID")
                .jsonPath("$.instance").isEqualTo(BASE_URL_BELT_CONTROLLER + "/" + invalidId);

        verifyNoInteractions(beltService);
    }

    @Test
    @DisplayName("PATCH /api/v1/belts/{id} when belt does not exist should return 404 not-found ProblemDetail")
    void updateBeltParameters_not_found_returns_404_problemDetail() {
        // arrange
        var beltId = UUID.randomUUID();
        var request = new BeltUpdateRequest(250, null);

        given(beltService.updateBeltParameters(beltId, 250, null))
                .willThrow(new ResourceNotFoundException("Belt", beltId));

        // act + assert
        client.patch()
                .uri(BASE_URL_BELT_CONTROLLER + "/{id}", beltId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_404_TITLE)
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.type").isEqualTo(PROBLEM_404_URI)
                .jsonPath("$.detail").isEqualTo("Belt not found: " + beltId)
                .jsonPath("$.instance").isEqualTo(BASE_URL_BELT_CONTROLLER + "/" + beltId);

        verify(beltService).updateBeltParameters(beltId, 250, null);
        verifyNoMoreInteractions(beltService);
    }

    @Test
    @DisplayName("PATCH /api/v1/belts/{id} on unexpected error should return 500 internal ProblemDetail")
    void updateBeltParameters_unexpected_returns_500_problemDetail() {
        // arrange
        var beltId = UUID.randomUUID();
        var request = new BeltUpdateRequest(250, null);

        given(beltService.updateBeltParameters(beltId, 250, null))
                .willThrow(new RuntimeException("boom"));

        // act + assert
        client.patch()
                .uri(BASE_URL_BELT_CONTROLLER + "/{id}", beltId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_500_TITLE)
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.type").isEqualTo(PROBLEM_500_URI)
                .jsonPath("$.detail").isEqualTo("Unexpected server error")
                .jsonPath("$.instance").isEqualTo(BASE_URL_BELT_CONTROLLER + "/" + beltId);

        verify(beltService).updateBeltParameters(beltId, 250, null);
        verifyNoMoreInteractions(beltService);
    }

    @Test
    @DisplayName("PATCH /api/v1/belts/{id} with malformed JSON should return 400 malformed-json ProblemDetail")
    void updateBeltParameters_malformed_json_returns_400_problemDetail() {
        // arrange
        var beltId = UUID.randomUUID();
        var malformedJson = "{";

        // act + assert
        client.patch()
                .uri(BASE_URL_BELT_CONTROLLER + "/{id}", beltId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(malformedJson)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_400_MALFORMED_REQUEST_TITLE)
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.type").isEqualTo(PROBLEM_400_MALFORMED_REQUEST_URI)
                .jsonPath("$.detail").isEqualTo("Request body is missing or malformed")
                .jsonPath("$.instance").isEqualTo(BASE_URL_BELT_CONTROLLER + "/" + beltId);

        verifyNoInteractions(beltService);
    }

    @Test
    @DisplayName("PATCH /api/v1/belts/{id} with missing body should return 400 malformed-json ProblemDetail")
    void updateBeltParameters_missing_body_returns_400_problemDetail() {
        // arrange
        var beltId = UUID.randomUUID();

        // act + assert
        client.patch()
                .uri(BASE_URL_BELT_CONTROLLER + "/{id}", beltId)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_400_MALFORMED_REQUEST_TITLE)
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.type").isEqualTo(PROBLEM_400_MALFORMED_REQUEST_URI)
                .jsonPath("$.detail").isEqualTo("Request body is missing or malformed")
                .jsonPath("$.instance").isEqualTo(BASE_URL_BELT_CONTROLLER + "/" + beltId);

        verifyNoInteractions(beltService);
    }
}
