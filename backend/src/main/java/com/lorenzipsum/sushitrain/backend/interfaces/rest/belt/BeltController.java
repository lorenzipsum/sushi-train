package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt;

import com.lorenzipsum.sushitrain.backend.application.belt.BeltService;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto.*;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.SeatStateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@RestController
@RequestMapping(path = BeltController.BASE_URL_BELT_CONTROLLER, produces = APPLICATION_JSON_VALUE)
@Tag(name = "Belts", description = "Operations for managing belts")
public class BeltController {
    static final String BASE_URL_BELT_CONTROLLER = "/api/v1/belts";

    private final BeltService service;
    private final BeltApiMapper mapper;
    private final BeltEventStreamBroker beltEventStreamBroker;

    public BeltController(BeltService service, BeltApiMapper mapper, BeltEventStreamBroker beltEventStreamBroker) {
        this.service = service;
        this.mapper = mapper;
        this.beltEventStreamBroker = beltEventStreamBroker;
    }

    @GetMapping()
    @Operation(
            summary = "Get all belts",
            description = "Returns all belts."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Belts returned",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = BeltDto.class)))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public List<BeltDto> getAllBelts() {
        var belts = service.getAllBelts();
        return belts.stream().map(mapper::toDto).toList();
    }

    @GetMapping(path = "/{id}")
    @Operation(
            summary = "Get a belt",
            description = "Returns the belt including its slots and seats."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Belt found",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FullBeltDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid parameter (e.g., id is not a UUID)",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Belt not found",
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
    public FullBeltDto getBelt(@PathVariable UUID id) {
        var belt = service.getBelt(id);
        return mapper.toFullDto(belt);
    }

    @GetMapping(path = "/{id}/snapshot", produces = APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get belt snapshot (slots with assigned plates)",
            description = "Returns a belt snapshot optimized for UI rendering. Includes belt parameters and all slots ordered by positionIndex. Each slot contains an optional plate block if a plate is assigned."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Snapshot returned",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BeltSnapshotDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid parameter (e.g., id is not a UUID)",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Belt not found",
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
    public BeltSnapshotDto getBeltSnapshot(@PathVariable UUID id) {
        var rows = service.getBeltSnapshotRows(id);
        return mapper.toSnapshotDto(rows);
    }

    @GetMapping(path = "/{id}/seats")
    @Operation(
            summary = "Get seat overview by belt",
            description = "Returns all seats of a belt with occupancy status."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Seat overview returned",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = SeatStateDto.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid parameter (e.g., id is not a UUID)",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Belt not found",
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
    public List<SeatStateDto> getSeatOverview(@PathVariable UUID id) {
        return mapper.toSeatStateDtos(service.getSeatStates(id));
    }

    @GetMapping(path = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamBeltEvents(@PathVariable UUID id) {
        service.getBelt(id);
        return beltEventStreamBroker.subscribe(id);
    }

    @PatchMapping(path = "/{id}", consumes = APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Update belt parameters",
            description = "Partially updates belt, for example speed parameters (at least one parameter must be provided)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Belt parameters updated",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BeltParamsDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid parameter (e.g., id is not a UUID) or validation failed",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Belt not found",
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
    public BeltParamsDto updateBeltParameters(
            @PathVariable UUID id,
            @RequestBody @Valid BeltUpdateRequest request
    ) {
        Belt belt = service.updateBeltParameters(id, request.tickIntervalMs(), request.speedSlotsPerTick());
        beltEventStreamBroker.publish(id, "belt-state-changed");
        return mapper.toParamsDto(belt);
    }

    @PostMapping(path = "/{id}/plates", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create plates and place them on the belt",
            description = "Creates the given number of plates and assigns them immediately to free belt slots. " +
                    "Requires enough free slots; also enforces a minimum gap of 5 slots between placed plates."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Plates created and placed",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreatedPlatesOnBeltResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid parameter or validation failed",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Belt not found",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Not enough free slots available",
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
    public CreatedPlatesOnBeltResponse createPlatesAndPlaceOnBelt(
            @PathVariable UUID id,
            @RequestBody @Valid CreatePlateAndPlaceOnBeltRequest request
    ) {
        CreatedPlatesOnBeltResponse response = mapper.toResponse(
                service.createPlatesAndPlaceOnBelt(id, mapper.toCommand(request))
        );
        beltEventStreamBroker.publish(id, "belt-state-changed");
        return response;
    }
}
