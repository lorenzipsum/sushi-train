package com.lorenzipsum.sushitrain.backend.domain.menu;

import java.util.Optional;
import java.util.UUID;

public interface MenuItemRepository {
    Optional<MenuItem> findById(UUID uuid);

    MenuItem save(MenuItem menuItem);
}
