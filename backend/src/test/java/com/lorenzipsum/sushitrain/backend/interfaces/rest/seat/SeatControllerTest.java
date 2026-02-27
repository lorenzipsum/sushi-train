package com.lorenzipsum.sushitrain.backend.interfaces.rest.seat;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.application.common.SeatAlreadyOccupiedException;
import com.lorenzipsum.sushitrain.backend.application.order.OrderService;
import com.lorenzipsum.sushitrain.backend.domain.common.OrderStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.lorenzipsum.sushitrain.backend.interfaces.rest.common.ControllerAdvice.*;
import static com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.SeatController.BASE_URL_SEAT_CONTROLLER;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@WebMvcTest(SeatController.class)
@AutoConfigureRestTestClient
class SeatControllerTest {

    @Autowired
    RestTestClient client;

    @MockitoBean
    private OrderService orderService;

    ///
    /// POST /api/v1/seats/{id}/occupy
    ///

    @Test
    @DisplayName("POST /api/v1/seats/{id}/occupy - Created")
    void occupySeat_created() {
        UUID seatId = UUID.randomUUID();

        var expectedSeatState = new SeatStateDto(seatId, "A1", 0, true);
        given(orderService.occupySeat(seatId)).willReturn(expectedSeatState);

        client.post().uri(BASE_URL_SEAT_CONTROLLER + "/{id}/occupy", seatId)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.seatId").isEqualTo(seatId.toString())
                .jsonPath("$.label").isEqualTo(expectedSeatState.label())
                .jsonPath("$.positionIndex").isEqualTo(expectedSeatState.positionIndex())
                .jsonPath("$.isOccupied").isEqualTo(expectedSeatState.isOccupied());
    }

    @Test
    @DisplayName("POST /api/v1/seats/{id}/occupy - Not Found")
    void occupySeat_notFound() {
        UUID seatId = UUID.randomUUID();

        given(orderService.occupySeat(seatId)).willThrow(new ResourceNotFoundException("Seat", seatId));

        client.post().uri(BASE_URL_SEAT_CONTROLLER + "/{id}/occupy", seatId)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_404_TITLE)
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.type").isEqualTo(PROBLEM_404_URI)
                .jsonPath("$.detail").isEqualTo("Seat not found: " + seatId)
                .jsonPath("$.instance").isEqualTo(BASE_URL_SEAT_CONTROLLER + "/" + seatId + "/occupy");
    }

    @Test
    @DisplayName("POST /api/v1/seats/{id}/occupy - Seat already occupied")
    void occupySeat_seatAlreadyOccupied() {
        UUID seatId = UUID.randomUUID();

        given(orderService.occupySeat(seatId)).willThrow(new SeatAlreadyOccupiedException(seatId));

        client.post().uri(BASE_URL_SEAT_CONTROLLER + "/{id}/occupy", seatId)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_409_TITLE)
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.type").isEqualTo(PROBLEM_409_URI)
                .jsonPath("$.detail").isEqualTo("Seat already occupied: " + seatId)
                .jsonPath("$.instance").isEqualTo(BASE_URL_SEAT_CONTROLLER + "/" + seatId + "/occupy");
    }

    @Test
    @DisplayName("POST /api/v1/seats/{id}/occupy - Bad Request (invalid UUID)")
    void occupySeat_badRequest_invalidUuid() {
        client.post().uri(BASE_URL_SEAT_CONTROLLER + "/{id}/occupy", "not-a-uuid")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_400_INVALID_PARAM_TITLE)
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.type").isEqualTo(PROBLEM_400_INVALID_PARAM_URI)
                .jsonPath("$.instance").isEqualTo(BASE_URL_SEAT_CONTROLLER + "/not-a-uuid/occupy");
    }

    ///
    /// GET  /api/v1/seats/{id}
    ///

    @Test
    @DisplayName("GET /api/v1/seats/{id} - OK")
    void getSeatState_ok() {
        UUID seatId = UUID.randomUUID();

        var expectedSeatState = new SeatStateDto(seatId, "A1", 0, false);
        given(orderService.getSeatState(seatId)).willReturn(expectedSeatState);

        client.get().uri(BASE_URL_SEAT_CONTROLLER + "/{id}", seatId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.seatId").isEqualTo(seatId.toString())
                .jsonPath("$.label").isEqualTo(expectedSeatState.label())
                .jsonPath("$.positionIndex").isEqualTo(expectedSeatState.positionIndex())
                .jsonPath("$.isOccupied").isEqualTo(expectedSeatState.isOccupied());
    }

    @Test
    @DisplayName("GET /api/v1/seats/{id} - Not Found")
    void getSeatState_notFound() {
        UUID seatId = UUID.randomUUID();

        given(orderService.getSeatState(seatId)).willThrow(new ResourceNotFoundException("Seat", seatId));

        client.get().uri(BASE_URL_SEAT_CONTROLLER + "/{id}", seatId)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_404_TITLE)
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.type").isEqualTo(PROBLEM_404_URI)
                .jsonPath("$.detail").isEqualTo("Seat not found: " + seatId)
                .jsonPath("$.instance").isEqualTo(BASE_URL_SEAT_CONTROLLER + "/" + seatId);
    }

    @Test
    @DisplayName("GET /api/v1/seats/{id} - Bad Request (invalid UUID)")
    void getSeatState_badRequest_invalidUuid() {
        client.get().uri(BASE_URL_SEAT_CONTROLLER + "/{id}", "not-a-uuid")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_400_INVALID_PARAM_TITLE)
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.type").isEqualTo(PROBLEM_400_INVALID_PARAM_URI)
                .jsonPath("$.instance").isEqualTo(BASE_URL_SEAT_CONTROLLER + "/not-a-uuid");
    }

    ///
    /// POST /api/v1/seats/{id}/order-lines
    ///

    @Test
    @DisplayName("POST /api/v1/seats/{id}/order-lines - Created")
    void pickPlate_created() {
        UUID seatId = UUID.randomUUID();
        UUID plateId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");

        var expectedSeatOrderDto = new SeatOrderDto(
                seatId,
                "A1",
                1,
                true,
                new OrderSummaryDto(
                        orderId,
                        seatId,
                        OrderStatus.OPEN,
                        createdAt,
                        null,
                        List.of(
                                new OrderLineDto("Salmon Nigiri", PlateTier.GREEN, 100),
                                new OrderLineDto("Tuna Roll", PlateTier.RED, 100)
                        ),
                        200
                )
        );

        given(orderService.pickPlate(eq(seatId), eq(plateId))).willReturn(expectedSeatOrderDto);

        client.post()
                .uri(BASE_URL_SEAT_CONTROLLER + "/{id}/order-lines", seatId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PickPlateRequest(plateId))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.seatId").isEqualTo(seatId.toString())
                .jsonPath("$.label").isEqualTo("A1")
                .jsonPath("$.positionIndex").isEqualTo(1)
                .jsonPath("$.isOccupied").isEqualTo(true)
                .jsonPath("$.orderSummary.orderId").isEqualTo(orderId.toString())
                .jsonPath("$.orderSummary.seatId").isEqualTo(seatId.toString())
                .jsonPath("$.orderSummary.status").isEqualTo(OrderStatus.OPEN.name())
                .jsonPath("$.orderSummary.createdAt").isEqualTo(createdAt.toString())
                .jsonPath("$.orderSummary.lines.length()").isEqualTo(2)
                .jsonPath("$.orderSummary.lines[0].menuItemName").isEqualTo("Salmon Nigiri")
                .jsonPath("$.orderSummary.lines[0].plateTier").isEqualTo(PlateTier.GREEN.name())
                .jsonPath("$.orderSummary.lines[0].price").isEqualTo(100)
                .jsonPath("$.orderSummary.lines[1].menuItemName").isEqualTo("Tuna Roll")
                .jsonPath("$.orderSummary.lines[1].plateTier").isEqualTo(PlateTier.RED.name())
                .jsonPath("$.orderSummary.lines[1].price").isEqualTo(100)
                .jsonPath("$.orderSummary.totalPrice").isEqualTo(200);

        verify(orderService).pickPlate(eq(seatId), eq(plateId));
    }

    @Test
    @DisplayName("POST /api/v1/seats/{id}/order-lines - Not Found")
    void pickPlate_notFound() {
        UUID seatId = UUID.randomUUID();
        UUID plateId = UUID.randomUUID();

        given(orderService.pickPlate(eq(seatId), eq(plateId)))
                .willThrow(new ResourceNotFoundException("Seat", seatId));

        client.post()
                .uri(BASE_URL_SEAT_CONTROLLER + "/{id}/order-lines", seatId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PickPlateRequest(plateId))
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_404_TITLE)
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.type").isEqualTo(PROBLEM_404_URI)
                .jsonPath("$.detail").isEqualTo("Seat not found: " + seatId)
                .jsonPath("$.instance").isEqualTo(BASE_URL_SEAT_CONTROLLER + "/" + seatId + "/order-lines");

        verify(orderService).pickPlate(eq(seatId), eq(plateId));
    }

    @Test
    @DisplayName("POST /api/v1/seats/{id}/order-lines - Conflict")
    void pickPlate_conflict() {
        UUID seatId = UUID.randomUUID();
        UUID plateId = UUID.randomUUID();

        // You likely have a more specific exception here (SeatNotOccupiedException, PlateNotPickableException, etc.)
        // Replace SeatAlreadyOccupiedException with the real one when available.
        given(orderService.pickPlate(eq(seatId), eq(plateId)))
                .willThrow(new SeatAlreadyOccupiedException(seatId)); // placeholder

        client.post()
                .uri(BASE_URL_SEAT_CONTROLLER + "/{id}/order-lines", seatId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PickPlateRequest(plateId))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_409_TITLE)
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.type").isEqualTo(PROBLEM_409_URI)
                .jsonPath("$.detail").isEqualTo("Seat already occupied: " + seatId)
                .jsonPath("$.instance").isEqualTo(BASE_URL_SEAT_CONTROLLER + "/" + seatId + "/order-lines");

        verify(orderService).pickPlate(eq(seatId), eq(plateId));
    }

    @Test
    @DisplayName("POST /api/v1/seats/{id}/order-lines - Bad Request (invalid seat UUID)")
    void pickPlate_badRequest_invalidSeatUuid() {
        UUID plateId = UUID.randomUUID();

        client.post()
                .uri(BASE_URL_SEAT_CONTROLLER + "/{id}/order-lines", "not-a-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PickPlateRequest(plateId))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_400_INVALID_PARAM_TITLE)
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.type").isEqualTo(PROBLEM_400_INVALID_PARAM_URI)
                .jsonPath("$.detail").isEqualTo("Parameter 'id' must be a UUID")
                .jsonPath("$.instance").isEqualTo(BASE_URL_SEAT_CONTROLLER + "/not-a-uuid/order-lines");

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("POST /api/v1/seats/{id}/order-lines - Bad Request (missing plateId)")
    void pickPlate_badRequest_missingPlateId() {
        UUID seatId = UUID.randomUUID();

        client.post()
                .uri(BASE_URL_SEAT_CONTROLLER + "/{id}/order-lines", seatId)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_400_VALIDATION_FAILED_TITLE)
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.type").isEqualTo(PROBLEM_400_VALIDATION_FAILED_URI)
                .jsonPath("$.detail").isEqualTo("One or more fields are invalid")
                .jsonPath("$.errors.plateId").isEqualTo("must not be null")
                .jsonPath("$.instance").isEqualTo(BASE_URL_SEAT_CONTROLLER + "/" + seatId + "/order-lines");

        verifyNoInteractions(orderService);
    }
}
