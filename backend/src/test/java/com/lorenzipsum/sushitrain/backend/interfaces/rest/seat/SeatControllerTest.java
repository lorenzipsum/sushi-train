package com.lorenzipsum.sushitrain.backend.interfaces.rest.seat;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.application.common.SeatAlreadyOccupiedException;
import com.lorenzipsum.sushitrain.backend.application.common.SeatNotOccupiedException;
import com.lorenzipsum.sushitrain.backend.application.order.OrderService;
import com.lorenzipsum.sushitrain.backend.domain.common.OrderStatus;
import com.lorenzipsum.sushitrain.backend.domain.common.PlateTier;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
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
                .jsonPath("$.title").isEqualTo(PROBLEM_409_SEAT_ALREADY_OCCUPIED_TITLE)
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.type").isEqualTo(PROBLEM_409_SEAT_ALREADY_OCCUPIED_URI)
                .jsonPath("$.detail").isEqualTo("Seat " + seatId + " already has an open order.")
                .jsonPath("$.errorCode").isEqualTo("SEAT_ALREADY_OCCUPIED")
                .jsonPath("$.seatId").isEqualTo(seatId.toString())
                .jsonPath("$.action").isEqualTo("checkout-seat-first")
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
        UUID orderId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");

        var expectedSeatOrderDto = new SeatOrderDto(
                seatId,
                "A1",
                0,
                true,
                new OrderSummaryDto(
                        orderId,
                        seatId,
                        OrderStatus.OPEN,
                        createdAt,
                        null,
                        List.of(
                                new OrderLineDto("Salmon Nigiri", PlateTier.GREEN, 100)
                        ),
                        100
                )
        );

        given(orderService.getSeatState(seatId)).willReturn(expectedSeatOrderDto);

        client.get().uri(BASE_URL_SEAT_CONTROLLER + "/{id}", seatId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.seatId").isEqualTo(seatId.toString())
                .jsonPath("$.label").isEqualTo("A1")
                .jsonPath("$.positionIndex").isEqualTo(0)
                .jsonPath("$.isOccupied").isEqualTo(true)
                .jsonPath("$.orderSummary.orderId").isEqualTo(orderId.toString())
                .jsonPath("$.orderSummary.seatId").isEqualTo(seatId.toString())
                .jsonPath("$.orderSummary.status").isEqualTo(OrderStatus.OPEN.name())
                .jsonPath("$.orderSummary.createdAt").isEqualTo(createdAt.toString())
                .jsonPath("$.orderSummary.lines.length()").isEqualTo(1)
                .jsonPath("$.orderSummary.lines[0].menuItemName").isEqualTo("Salmon Nigiri")
                .jsonPath("$.orderSummary.lines[0].plateTier").isEqualTo(PlateTier.GREEN.name())
                .jsonPath("$.orderSummary.lines[0].price").isEqualTo(100)
                .jsonPath("$.orderSummary.totalPrice").isEqualTo(100);

        verify(orderService).getSeatState(seatId);
    }

    @Test
    @DisplayName("GET /api/v1/seats/{id} - OK (no order assigned)")
    void getSeatState_ok_noOrderAssigned() {
        UUID seatId = UUID.randomUUID();

        var expectedSeatOrderDto = new SeatOrderDto(
                seatId,
                "A2",
                1,
                false,
                null
        );

        given(orderService.getSeatState(seatId)).willReturn(expectedSeatOrderDto);

        client.get().uri(BASE_URL_SEAT_CONTROLLER + "/{id}", seatId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.seatId").isEqualTo(seatId.toString())
                .jsonPath("$.label").isEqualTo("A2")
                .jsonPath("$.positionIndex").isEqualTo(1)
                .jsonPath("$.isOccupied").isEqualTo(false)
                .jsonPath("$.orderSummary").doesNotExist();

        verify(orderService).getSeatState(seatId);
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
    @DisplayName("POST /api/v1/seats/{id}/order-lines - Conflict (same plate picked twice)")
    void pickPlate_conflict_duplicatePlate() {
        UUID seatId = UUID.randomUUID();
        UUID plateId = UUID.randomUUID();

        given(orderService.pickPlate(eq(seatId), eq(plateId)))
                .willThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        client.post()
                .uri(BASE_URL_SEAT_CONTROLLER + "/{id}/order-lines", seatId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PickPlateRequest(plateId))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_409_STATE_CONFLICT_TITLE)
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.type").isEqualTo(PROBLEM_409_STATE_CONFLICT_URI)
                .jsonPath("$.detail").isEqualTo("Request conflicts with current resource state.")
                .jsonPath("$.errorCode").isEqualTo("RESOURCE_STATE_CONFLICT")
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

    ///
    /// POST /api/v1/seats/{id}/checkout
    ///

    @Test
    @DisplayName("POST /api/v1/seats/{id}/checkout - OK")
    void checkout_ok() {
        UUID seatId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant closedAt = Instant.parse("2026-01-01T00:30:00Z");

        var expectedSeatOrderDto = new SeatOrderDto(
                seatId,
                "A1",
                0,
                false,
                new OrderSummaryDto(
                        orderId,
                        seatId,
                        OrderStatus.CHECKED_OUT,
                        createdAt,
                        closedAt,
                        List.of(new OrderLineDto("Salmon Nigiri", PlateTier.GREEN, 100)),
                        100
                )
        );

        given(orderService.checkout(seatId)).willReturn(expectedSeatOrderDto);

        client.post()
                .uri(BASE_URL_SEAT_CONTROLLER + "/{id}/checkout", seatId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.seatId").isEqualTo(seatId.toString())
                .jsonPath("$.label").isEqualTo("A1")
                .jsonPath("$.positionIndex").isEqualTo(0)
                .jsonPath("$.isOccupied").isEqualTo(false)
                .jsonPath("$.orderSummary.orderId").isEqualTo(orderId.toString())
                .jsonPath("$.orderSummary.status").isEqualTo(OrderStatus.CHECKED_OUT.name())
                .jsonPath("$.orderSummary.closedAt").isEqualTo(closedAt.toString())
                .jsonPath("$.orderSummary.totalPrice").isEqualTo(100);

        verify(orderService).checkout(seatId);
    }

    @Test
    @DisplayName("POST /api/v1/seats/{id}/checkout - Not Found")
    void checkout_notFound() {
        UUID seatId = UUID.randomUUID();

        given(orderService.checkout(seatId)).willThrow(new ResourceNotFoundException("Seat", seatId));

        client.post()
                .uri(BASE_URL_SEAT_CONTROLLER + "/{id}/checkout", seatId)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_404_TITLE)
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.type").isEqualTo(PROBLEM_404_URI)
                .jsonPath("$.detail").isEqualTo("Seat not found: " + seatId)
                .jsonPath("$.instance").isEqualTo(BASE_URL_SEAT_CONTROLLER + "/" + seatId + "/checkout");

        verify(orderService).checkout(seatId);
    }

    @Test
    @DisplayName("POST /api/v1/seats/{id}/checkout - Conflict (seat not occupied)")
    void checkout_conflict_seatNotOccupied() {
        UUID seatId = UUID.randomUUID();

        given(orderService.checkout(seatId)).willThrow(new SeatNotOccupiedException(seatId));

        client.post()
                .uri(BASE_URL_SEAT_CONTROLLER + "/{id}/checkout", seatId)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_409_SEAT_NOT_OCCUPIED_TITLE)
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.type").isEqualTo(PROBLEM_409_SEAT_NOT_OCCUPIED_URI)
                .jsonPath("$.detail").isEqualTo("Seat " + seatId + " has no open order.")
                .jsonPath("$.errorCode").isEqualTo("SEAT_NOT_OCCUPIED")
                .jsonPath("$.seatId").isEqualTo(seatId.toString())
                .jsonPath("$.action").isEqualTo("occupy-seat-first")
                .jsonPath("$.instance").isEqualTo(BASE_URL_SEAT_CONTROLLER + "/" + seatId + "/checkout");

        verify(orderService).checkout(seatId);
    }

    @Test
    @DisplayName("POST /api/v1/seats/{id}/checkout - Bad Request (invalid UUID)")
    void checkout_badRequest_invalidUuid() {
        client.post()
                .uri(BASE_URL_SEAT_CONTROLLER + "/{id}/checkout", "not-a-uuid")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo(PROBLEM_400_INVALID_PARAM_TITLE)
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.type").isEqualTo(PROBLEM_400_INVALID_PARAM_URI)
                .jsonPath("$.detail").isEqualTo("Parameter 'id' must be a UUID")
                .jsonPath("$.instance").isEqualTo(BASE_URL_SEAT_CONTROLLER + "/not-a-uuid/checkout");

        verifyNoInteractions(orderService);
    }
}
