package com.lorenzipsum.sushitrain.backend.application.menu;

import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItem;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItemRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MenuItemService {
    private final MenuItemRepository repository;

    public MenuItemService(MenuItemRepository repository) {
        this.repository = repository;
    }

    public MenuItem getMenuItem(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
    }
}
