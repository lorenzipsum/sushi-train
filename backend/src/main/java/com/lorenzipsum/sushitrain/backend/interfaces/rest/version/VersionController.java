package com.lorenzipsum.sushitrain.backend.interfaces.rest.version;

import com.lorenzipsum.sushitrain.backend.infrastructure.version.VersionInfo;
import com.lorenzipsum.sushitrain.backend.infrastructure.version.VersionInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@RestController
@RequestMapping(path = VersionController.BASE_URL_VERSION_CONTROLLER, produces = APPLICATION_JSON_VALUE)
@Tag(name = "Version", description = "Build and version metadata")
public class VersionController {
    static final String BASE_URL_VERSION_CONTROLLER = "/api/version";

    private final VersionInfoService versionInfoService;

    public VersionController(VersionInfoService versionInfoService) {
        this.versionInfoService = versionInfoService;
    }

    @GetMapping
    @Operation(
            summary = "Get backend version metadata",
            description = "Returns safe, read-only build and environment metadata for UI system info."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Version metadata returned",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VersionInfo.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public VersionInfo getVersion() {
        return versionInfoService.getVersionInfo();
    }
}
