package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt;

import com.lorenzipsum.sushitrain.backend.application.belt.BeltService;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@RestController
@RequestMapping(path = BeltController.BASE_URL_BELT_CONTROLLER, produces = APPLICATION_JSON_VALUE)
@Tag(name = "Belts", description = "Operations for managing belts")
public class BeltController {

    public static final String BASE_URL_BELT_CONTROLLER = "/api/v1/belts";

    private final BeltService service;
    private final BeltDtoMapper mapper;
    private final BeltSnapshotDtoMapper snapshotMapper;

    public BeltController(BeltService service, BeltDtoMapper mapper, BeltSnapshotDtoMapper snapshotMapper) {
        this.service = service;
        this.mapper = mapper;
        this.snapshotMapper = snapshotMapper;
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
                            schema = @Schema(implementation = BeltDto.class))
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
        return snapshotMapper.toDto(rows);
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
        return mapper.toParamsDto(belt);
    }
}