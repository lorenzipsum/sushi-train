package com.lorenzipsum.sushitrain.backend.interfaces.rest.seat;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.application.common.SeatAlreadyOccupiedException;
import com.lorenzipsum.sushitrain.backend.application.order.OrderService;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.SeatStateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.UUID;

import static com.lorenzipsum.sushitrain.backend.interfaces.rest.common.ControllerAdvice.*;
import static com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.SeatController.BASE_URL_SEAT_CONTROLLER;
import static org.mockito.BDDMockito.given;

@WebMvcTest(SeatController.class)
@AutoConfigureRestTestClient
class SeatControllerTest {

    @Autowired
    RestTestClient client;

    @MockitoBean
    private OrderService orderService;

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
}