package com.lorenzipsum.sushitrain.backend.interfaces.rest.seat;

import com.lorenzipsum.sushitrain.backend.application.order.OrderService;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.SeatStateDto;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.SeatController.BASE_URL_SEAT_CONTROLLER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = BASE_URL_SEAT_CONTROLLER, produces = APPLICATION_JSON_VALUE)
public class SeatController {
    static final String BASE_URL_SEAT_CONTROLLER = "/api/v1/seats";

    private final OrderService service;

    public SeatController(OrderService service) {
        this.service = service;
    }

    @PostMapping(path = "/{id}/occupy")
    public SeatStateDto occupySeat(@PathVariable UUID id) {
        return service.occupySeat(id);
    }

    @GetMapping(path = "/{id}")
    public SeatStateDto getSeatState(@PathVariable UUID id) {
        return service.getSeatState(id);
    }

// TODO
//    POST /api/v1/seats/{id}/order-lines body { "plateId": "..." }
//    POST /api/v1/seats/{id}/checkout
}
