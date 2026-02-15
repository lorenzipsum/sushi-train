package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt;

import com.lorenzipsum.sushitrain.backend.application.belt.BeltService;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto.BeltDtoMapper;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto.BeltParamsDto;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto.BeltUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

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

    public BeltController(BeltService service, BeltDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
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
