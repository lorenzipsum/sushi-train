package com.lorenzipsum.sushitrain.backend.interfaces.rest.plate;

import com.lorenzipsum.sushitrain.backend.application.plate.PlateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plates")
@Tag(name = "Plate Controller")
public class PlateController {

    private final PlateService service;
    private final PlateDtoMapper mapper;

    public PlateController(PlateService service, PlateDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a plate by id", description = "Returns a single plate or 404 if non existing.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Plate found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PlateDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid UUID format",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    description = "Plate not found",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "Unexpected error",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))})
    public PlateDto getPlate(@PathVariable UUID id) {
        return mapper.toDto(service.getPlate(id));
    }
}
