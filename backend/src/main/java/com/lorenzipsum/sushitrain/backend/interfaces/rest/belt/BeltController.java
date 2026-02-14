package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt;

import com.lorenzipsum.sushitrain.backend.application.belt.BeltService;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto.BeltDtoMapper;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto.BeltParamsDto;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.belt.dto.BeltUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = BeltController.BASE_URL_BELT_CONTROLLER, produces = APPLICATION_JSON_VALUE)
public class BeltController {
    public static final String BASE_URL_BELT_CONTROLLER = "/api/v1/belts";
    private final BeltService service;
    private final BeltDtoMapper mapper;

    public BeltController(BeltService service, BeltDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PatchMapping(path = "/{id}")
    public BeltParamsDto updateBeltParameters(@PathVariable UUID id, @RequestBody @Valid BeltUpdateRequest request) {
        Belt belt = service.updateBeltParameters(id, request.tickIntervalMs(), request.speedSlotsPerTick());
        return mapper.toParamsDto(belt);
    }
}
