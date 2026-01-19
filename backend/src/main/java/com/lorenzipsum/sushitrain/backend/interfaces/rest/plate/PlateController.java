package com.lorenzipsum.sushitrain.backend.interfaces.rest.plate;

import com.lorenzipsum.sushitrain.backend.application.plate.PlateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plates")
public class PlateController {

    private final PlateService service;
    private final PlateDtoMapper mapper;

    public PlateController(PlateService service, PlateDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public PlateDto getPlate(@PathVariable UUID id) {
        return mapper.toDto(service.getPlate(id));
    }
}
