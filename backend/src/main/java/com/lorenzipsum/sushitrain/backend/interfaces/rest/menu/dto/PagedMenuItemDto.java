package com.lorenzipsum.sushitrain.backend.interfaces.rest.menu.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;

public class PagedMenuItemDto extends PagedModel<MenuItemDto> {
    public PagedMenuItemDto(Page<MenuItemDto> page) {
        super(page);
    }
}
