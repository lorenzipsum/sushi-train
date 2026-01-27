package com.lorenzipsum.sushitrain.backend.interfaces.rest.menu.dto;

import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItem;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.common.dto.MoneyYenMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = MoneyYenMapper.class)
public interface MenuItemDtoMapper {
    MenuItemDto toDto(MenuItem menuItem);
}
