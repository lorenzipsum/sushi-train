package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItem;
import com.lorenzipsum.sushitrain.backend.domain.menu.MenuItemRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.MenuItemMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.MenuItemJpaDao;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaMenuItemRepository implements MenuItemRepository {

    private final MenuItemJpaDao dao;
    private final MenuItemMapper mapper;

    public JpaMenuItemRepository(MenuItemJpaDao dao, MenuItemMapper mapper) {
        this.dao = dao;
        this.mapper = mapper;
    }

    @Override
    public Optional<MenuItem> findById(UUID uuid) {
        return dao.findById(uuid).map(mapper::toDomain);
    }

    @Override
    public MenuItem save(MenuItem menuItem) {
        var saved = dao.save(mapper.toEntity(menuItem));
        return mapper.toDomain(saved);
    }
}