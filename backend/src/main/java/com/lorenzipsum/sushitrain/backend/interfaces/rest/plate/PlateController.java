package com.lorenzipsum.sushitrain.backend.interfaces.rest.plate;

import com.lorenzipsum.sushitrain.backend.application.plate.PlateService;
import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.plate.dto.CreatePlateRequest;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.plate.dto.PlateDto;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.plate.dto.PlateDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@RestController
@RequestMapping("/api/v1/plates")
@Tag(name = "Plates", description = "Operations for managing plates on the sushi belt")
public class PlateController {

    private final PlateService service;
    private final PlateDtoMapper mapper;

    public PlateController(PlateService service, PlateDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping(path = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a plate by id", description = "Returns a single plate. If the id does not exist, returns a ProblemDetail (404).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plate found",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlateDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid UUID format",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Plate not found",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))})
    public PlateDto getPlate(
            @Parameter(description = "Plate id", required = true, example = "a22b5bd2-285f-42eb-889a-8d2dd1f2d6c7")
            @PathVariable UUID id) {
        return mapper.toDto(service.getPlate(id));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new plate",
            description = "Creates a plate and returns the created resource. Validation errors return ProblemDetail (400)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Plate created",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlateDto.class))
            ),
            @ApiResponse(responseCode = "400", description = """
                    Bad request:
                    - malformed JSON
                    - bean validation failed
                    """,
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public PlateDto createPlate(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true, description = "Plate creation request",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreatePlateRequest.class)))
            @Valid @RequestBody CreatePlateRequest request) {
        MoneyYen optionalPrice = request.priceAtCreation() != null ? MoneyYen.of(request.priceAtCreation()) : null;

        var plate = service.createPlate(
                request.menuItemId(),
                request.tierSnapshot(),
                optionalPrice,
                request.expiresAt());

        return mapper.toDto(plate);
    }

    @PostMapping(path = "/{id}/expire", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Expire a plate",
            description = "Expires a plate immediately. If the plate does not exist, returns a ProblemDetail (404).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plate expired",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlateDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid UUID format",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Plate not found",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))})
    public PlateDto expirePlate(
            @Parameter(description = "Plate id", required = true, example = "a22b5bd2-285f-42eb-889a-8d2dd1f2d6c7")
            @PathVariable UUID id) {
        return mapper.toDto(service.expirePlate(id));
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all plates", description = "Returns a list of all plates.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of plates",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PagedModel.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))})
    public PagedModel<PlateDto> getAllPlates(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(200) int size) {

        return new PagedModel<>(service.getAllPlates(PageRequest.of(page, size)).map(mapper::toDto));
    }
}
