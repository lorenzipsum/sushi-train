package com.lorenzipsum.sushitrain.backend.interfaces.rest.seat;

import com.lorenzipsum.sushitrain.backend.application.order.OrderService;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.PickPlateRequest;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.SeatOrderDto;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.SeatStateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.SeatController.BASE_URL_SEAT_CONTROLLER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@RestController
@RequestMapping(path = BASE_URL_SEAT_CONTROLLER, produces = APPLICATION_JSON_VALUE)
@Tag(name = "Seats", description = "Operations for seat occupancy and ordering")
public class SeatController {
    static final String BASE_URL_SEAT_CONTROLLER = "/api/v1/seats";

    private final OrderService service;

    public SeatController(OrderService service) {
        this.service = service;
    }

    @PostMapping(path = "/{id}/occupy")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Occupy a seat (create an open order)",
            description = "Creates a new open order and assigns it to the given seat. " +
                    "Fails if the seat is already occupied."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Seat occupied and new order created",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SeatStateDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid parameter (e.g., id is not a UUID)",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Seat not found",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Seat already occupied",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public SeatStateDto occupySeat(@PathVariable UUID id) {
        return service.occupySeat(id);
    }

    @GetMapping(path = "/{id}")
    @Operation(
            summary = "Get seat state",
            description = "Returns the seat state including its occupancy and open order summary (if occupied)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Seat state returned",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SeatStateDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid parameter (e.g., id is not a UUID)",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Seat not found",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public SeatOrderDto getSeatStateAndOrderSummary(@PathVariable UUID id) {
        return service.getSeatState(id);
    }

    @PostMapping(path = "/{id}/order-lines", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Pick a plate (create an order line)",
            description = "Picks the given plate from the belt and adds it as an order line to the current open order of the seat. " +
                    "Fails if the seat is not occupied or the plate is not pickable."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Plate picked and order line created",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SeatOrderDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid parameter (e.g., id is not a UUID) or validation failed",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Seat not found (or referenced plate not found)",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Seat not occupied or plate not pickable",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public SeatOrderDto pickPlate(
            @PathVariable UUID id,
            @org.springframework.web.bind.annotation.RequestBody @Valid PickPlateRequest request
    ) {
        return service.pickPlate(id, request.plateId());
    }

    @PostMapping(path = "/{id}/checkout")
    @Operation(
            summary = "Checkout seat order",
            description = "Checks out the current open order for the given seat."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order checked out",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SeatOrderDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid parameter (e.g., id is not a UUID)",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Seat not found",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public SeatOrderDto checkout(@PathVariable UUID id) {
        return service.checkout(id);
    }
}
