package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt;

import com.lorenzipsum.sushitrain.backend.application.belt.BeltService;
import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.belt.SeatSpec;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.projection.BeltSlotPlateRow;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto.BeltDtoMapperImpl;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto.BeltSnapshotDtoMapper;
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

@Import({BeltDtoMapperImpl.class, BeltSnapshotDtoMapper.class})
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
    void updateBeltParameters_valid_updates_ok(BeltUpdateRequest updateRequest, Integer expectedTicks, Integer expectedSpeed) {
        var belt = Belt.create("Test Belt", 10, List.of(new SeatSpec("1", 3)));
        var now = Instant.now();
        if (expectedTicks != null) belt.setTickIntervalMs(expectedTicks, now);
        if (expectedSpeed != null) belt.setSpeedSlotsPerTick(expectedSpeed, now);

        given(beltService.updateBeltParameters(belt.getId(), expectedTicks, expectedSpeed)).willReturn(belt);

        var body = client.patch().uri(BASE_URL_BELT_CONTROLLER + "/{id}", belt.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(belt.getId().toString());

        if (expectedTicks != null) body.jsonPath("$.tickIntervalMs").isEqualTo(expectedTicks);
        if (expectedSpeed != null) body.jsonPath("$.speedSlotsPerTick").isEqualTo(expectedSpeed);

        verify(beltService).updateBeltParameters(belt.getId(), expectedTicks, expectedSpeed);
        verifyNoMoreInteractions(beltService);
    }

    @ParameterizedTest
    @MethodSource("invalidUpdates")
    @DisplayName("PATCH /api/v1/belts/{id} with invalid parameters should return 400 with ProblemDetail")
    void updateBeltParameters_invalid_updates_not_ok(BeltUpdateRequest updateRequest, String errorKey) {
        var beltId = UUID.randomUUID();

        client.patch().uri(BASE_URL_BELT_CONTROLLER + "/{id}", beltId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateRequest)
                .exchange()
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
        var invalidId = "not-a-uuid";
        var request = new BeltUpdateRequest(250, null);

        client.patch().uri(BASE_URL_BELT_CONTROLLER + "/{id}", invalidId)
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
        var beltId = UUID.randomUUID();
        var request = new BeltUpdateRequest(250, null);

        given(beltService.updateBeltParameters(beltId, 250, null)).willThrow(new ResourceNotFoundException("Belt", beltId));

        client.patch().uri(BASE_URL_BELT_CONTROLLER + "/{id}", beltId)
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
        var beltId = UUID.randomUUID();
        var request = new BeltUpdateRequest(250, null);

        given(beltService.updateBeltParameters(beltId, 250, null)).willThrow(new RuntimeException("boom"));

        client.patch().uri(BASE_URL_BELT_CONTROLLER + "/{id}", beltId)
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
        var beltId = UUID.randomUUID();
        var malformedJson = "{";

        client.patch().uri(BASE_URL_BELT_CONTROLLER + "/{id}", beltId)
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
        var beltId = UUID.randomUUID();

        client.patch().uri(BASE_URL_BELT_CONTROLLER + "/{id}", beltId)
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

    // -----------------------
    // GET /api/v1/belts/{id}
    // -----------------------

    @Test
    @DisplayName("GET /api/v1/belts/{id} should return 200 and BeltDto when found")
    void getBelt_ok_returns_200_and_dto() {
        var belt = Belt.create("Test Belt", 10, List.of(new SeatSpec("A1", 1), new SeatSpec("B2", 7)));
        given(beltService.getBelt(belt.getId())).willReturn(belt);

        client.get().uri(BASE_URL_BELT_CONTROLLER + "/{id}", belt.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(belt.getId().toString())
                .jsonPath("$.name").isEqualTo(belt.getName())
                .jsonPath("$.slotCount").isEqualTo(belt.getSlotCount())
                .jsonPath("$.baseRotationOffset").isEqualTo(belt.getBaseRotationOffset())
                .jsonPath("$.tickIntervalMs").isEqualTo(belt.getTickIntervalMs())
                .jsonPath("$.speedSlotsPerTick").isEqualTo(belt.getSpeedSlotsPerTick())
                .jsonPath("$.offsetStartedAt").exists()

                // slots array sanity
                .jsonPath("$.slots").isArray()
                .jsonPath("$.slots.length()").isEqualTo(belt.getSlotCount())
                .jsonPath("$.slots[0].id").exists()
                .jsonPath("$.slots[0].positionIndex").isEqualTo(0)
                .jsonPath("$.slots[0].plateId").isEmpty()
                .jsonPath("$.slots[" + (belt.getSlotCount() - 1) + "].positionIndex").isEqualTo(belt.getSlotCount() - 1)

                // seats array sanity
                .jsonPath("$.seats").isArray()
                .jsonPath("$.seats.length()").isEqualTo(belt.getSeats().size())
                .jsonPath("$.seats[0].id").exists()
                .jsonPath("$.seats[0].positionIndex").exists()
                .jsonPath("$.seats[0].label").exists();

        verify(beltService).getBelt(belt.getId());
        verifyNoMoreInteractions(beltService);
    }

    @Test
    @DisplayName("GET /api/v1/belts/{id} with non-UUID id should return 400 invalid-parameter ProblemDetail")
    void getBelt_invalid_uuid_returns_400_problemDetail() {
        var invalidId = "not-a-uuid";

        client.get().uri(BASE_URL_BELT_CONTROLLER + "/{id}", invalidId)
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
    @DisplayName("GET /api/v1/belts/{id} when belt does not exist should return 404 not-found ProblemDetail")
    void getBelt_not_found_returns_404_problemDetail() {
        var beltId = UUID.randomUUID();
        given(beltService.getBelt(beltId)).willThrow(new ResourceNotFoundException("Belt", beltId));

        client.get().uri(BASE_URL_BELT_CONTROLLER + "/{id}", beltId)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_404_TITLE)
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.type").isEqualTo(PROBLEM_404_URI)
                .jsonPath("$.detail").isEqualTo("Belt not found: " + beltId)
                .jsonPath("$.instance").isEqualTo(BASE_URL_BELT_CONTROLLER + "/" + beltId);

        verify(beltService).getBelt(beltId);
        verifyNoMoreInteractions(beltService);
    }

    @Test
    @DisplayName("GET /api/v1/belts/{id} on unexpected error should return 500 internal ProblemDetail")
    void getBelt_unexpected_returns_500_problemDetail() {
        var beltId = UUID.randomUUID();
        given(beltService.getBelt(beltId)).willThrow(new RuntimeException("boom"));

        client.get().uri(BASE_URL_BELT_CONTROLLER + "/{id}", beltId)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_500_TITLE)
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.type").isEqualTo(PROBLEM_500_URI)
                .jsonPath("$.detail").isEqualTo("Unexpected server error")
                .jsonPath("$.instance").isEqualTo(BASE_URL_BELT_CONTROLLER + "/" + beltId);

        verify(beltService).getBelt(beltId);
        verifyNoMoreInteractions(beltService);
    }

    @Test
    @DisplayName("GET /api/v1/belts/{id}/snapshot returns 200 and contains belt params + slots")
    void getBeltSnapshot_ok() {
        var beltId = UUID.randomUUID();
        var slot0 = UUID.randomUUID();
        var slot1 = UUID.randomUUID();
        var plateId = UUID.randomUUID();
        var menuItemId = UUID.randomUUID();
        var startedAt = Instant.parse("2026-02-20T00:00:00Z");
        var expiresAt = Instant.parse("2026-02-20T02:00:00Z");

        var rows = List.of(
                new BeltSlotPlateRow(beltId, "Main Belt", 2, 10, startedAt, 900, 3, slot0, 0, null, null, null, null, null, null, null),
                new BeltSlotPlateRow(beltId, "Main Belt", 2, 10, startedAt, 900, 3, slot1, 1, plateId, menuItemId, "Salmon Nigiri", PlateTier.GREEN, MoneyYen.of(450), PlateStatus.ON_BELT, expiresAt)
        );

        given(beltService.getBeltSnapshotRows(beltId)).willReturn(rows);

        client.get()
                .uri(BASE_URL_BELT_CONTROLLER + "/{id}/snapshot", beltId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.beltId").isEqualTo(beltId.toString())
                .jsonPath("$.beltName").isEqualTo("Main Belt")
                .jsonPath("$.beltSlotCount").isEqualTo(2)
                .jsonPath("$.slots").isArray()
                .jsonPath("$.slots.length()").isEqualTo(2)
                .jsonPath("$.slots[0].positionIndex").isEqualTo(0)
                .jsonPath("$.slots[0].plate").doesNotExist()
                .jsonPath("$.slots[1].positionIndex").isEqualTo(1)
                .jsonPath("$.slots[1].plate.plateId").isEqualTo(plateId.toString())
                .jsonPath("$.slots[1].plate.menuItemId").isEqualTo(menuItemId.toString())
                .jsonPath("$.slots[1].plate.menuItemName").isEqualTo("Salmon Nigiri");

        verify(beltService).getBeltSnapshotRows(beltId);
        verifyNoMoreInteractions(beltService);
    }

    @Test
    @DisplayName("GET /api/v1/belts/{id}/snapshot with non-UUID id returns 400 invalid-parameter ProblemDetail")
    void getBeltSnapshot_invalid_uuid_returns_400_problemDetail() {
        var invalidId = "not-a-uuid";

        client.get()
                .uri(BASE_URL_BELT_CONTROLLER + "/{id}/snapshot", invalidId)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_400_INVALID_PARAM_TITLE)
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.type").isEqualTo(PROBLEM_400_INVALID_PARAM_URI)
                .jsonPath("$.detail").isEqualTo("Parameter 'id' must be a UUID")
                .jsonPath("$.instance").isEqualTo(BASE_URL_BELT_CONTROLLER + "/" + invalidId + "/snapshot");

        verifyNoInteractions(beltService);
    }

    @Test
    @DisplayName("GET /api/v1/belts/{id}/snapshot when belt does not exist returns 404 ProblemDetail")
    void getBeltSnapshot_not_found_returns_404_problemDetail() {
        var beltId = UUID.randomUUID();

        given(beltService.getBeltSnapshotRows(beltId)).willThrow(new ResourceNotFoundException("Belt", beltId));

        client.get()
                .uri(BASE_URL_BELT_CONTROLLER + "/{id}/snapshot", beltId)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_404_TITLE)
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.type").isEqualTo(PROBLEM_404_URI)
                .jsonPath("$.detail").isEqualTo("Belt not found: " + beltId)
                .jsonPath("$.instance").isEqualTo(BASE_URL_BELT_CONTROLLER + "/" + beltId + "/snapshot");

        verify(beltService).getBeltSnapshotRows(beltId);
        verifyNoMoreInteractions(beltService);
    }

    @Test
    @DisplayName("GET /api/v1/belts/{id}/snapshot on unexpected error returns 500 ProblemDetail")
    void getBeltSnapshot_unexpected_returns_500_problemDetail() {
        var beltId = UUID.randomUUID();

        given(beltService.getBeltSnapshotRows(beltId)).willThrow(new RuntimeException("boom"));

        client.get()
                .uri(BASE_URL_BELT_CONTROLLER + "/{id}/snapshot", beltId)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_500_TITLE)
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.type").isEqualTo(PROBLEM_500_URI)
                .jsonPath("$.detail").isEqualTo("Unexpected server error")
                .jsonPath("$.instance").isEqualTo(BASE_URL_BELT_CONTROLLER + "/" + beltId + "/snapshot");

        verify(beltService).getBeltSnapshotRows(beltId);
        verifyNoMoreInteractions(beltService);
    }
}