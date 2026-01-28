package com.lorenzipsum.sushitrain.backend.interfaces.rest.menu;

import com.lorenzipsum.sushitrain.backend.application.menu.MenuItemService;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.menu.dto.MenuItemDto;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.menu.dto.MenuItemDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@RestController
@RequestMapping("/api/v1/menu-items")
@Tag(name = "Menu Items", description = "Operations for managing menu items")
public class MenuItemController {

    private final MenuItemService service;
    private final MenuItemDtoMapper mapper;

    public MenuItemController(MenuItemService service, MenuItemDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping(path = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a menu item by id", description = "Returns a single menu item. If the id does not exist, returns a ProblemDetail (404).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu item found",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MenuItemDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid UUID format",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Menu item not found",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public MenuItemDto getMenuItem(@PathVariable UUID id) {
        return mapper.toDto(service.getMenuItem(id));
    }


    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all menu items", description = "Returns a paginated list of all menu items.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of menu items",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MenuItemDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public PagedModel<MenuItemDto> getAllMenuItems(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(200) int size) {

        return new PagedModel<>(service.getAllMenuItems(PageRequest.of(page, size)).map(mapper::toDto));
    }
}
