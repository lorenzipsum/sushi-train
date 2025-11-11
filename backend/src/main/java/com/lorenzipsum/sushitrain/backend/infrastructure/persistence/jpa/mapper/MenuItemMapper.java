package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItem;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.MenuItemEntity;
import org.springframework.stereotype.Component;

/**
 * Maps between domain MenuItem and JPA MenuItemEntity.
 * MoneyYen <-> INT column mapping lives here.
 */
@Component
public class MenuItemMapper {

    public MenuItem toDomain(MenuItemEntity e) {
        if (e == null) return null;
        return MenuItem.rehydrate(
                e.getId(),
                e.getName(),
                e.getDefaultTier(),
                new MoneyYen(e.getBasePriceYen()),
                e.getCreatedAt()
        );
    }

    public MenuItemEntity toEntity(MenuItem d) {
        if (d == null) return null;
        return new MenuItemEntity(
                d.getId(),
                d.getName(),
                d.getDefaultTier(),
                d.getBasePrice().getAmount(),
                d.getCreatedAt()
        );
    }
}
