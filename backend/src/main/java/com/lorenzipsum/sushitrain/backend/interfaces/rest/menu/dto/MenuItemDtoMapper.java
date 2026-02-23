package com.lorenzipsum.sushitrain.backend.interfaces.rest.menu.dto;

import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItem;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.common.dto.YenAmountMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = YenAmountMapper.class)
public interface MenuItemDtoMapper {
    MenuItemDto toDto(MenuItem menuItem);
}
