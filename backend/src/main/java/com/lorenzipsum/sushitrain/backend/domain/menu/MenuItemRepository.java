package com.lorenzipsum.sushitrain.backend.domain.menu;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface MenuItemRepository {
    Optional<MenuItem> findById(UUID uuid);

    MenuItem save(MenuItem menuItem);

    Page<MenuItem> findAll(Pageable pageable);
}
