package com.lorenzipsum.sushitrain.backend.interfaces.rest.plate.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;

public class PagedPlateDto extends PagedModel<PlateDto> {
    public PagedPlateDto(Page<PlateDto> page) {
        super(page);
    }
}
