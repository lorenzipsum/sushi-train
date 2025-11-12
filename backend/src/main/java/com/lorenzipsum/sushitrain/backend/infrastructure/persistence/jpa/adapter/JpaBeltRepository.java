package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltJpaDao;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaBeltRepository implements BeltRepository {

    private final BeltJpaDao dao;
    private final BeltMapper mapper;

    public JpaBeltRepository(BeltJpaDao dao, BeltMapper mapper) {
        this.dao = dao;
        this.mapper = mapper;
    }

    @Override
    public Optional<Belt> findById(UUID uuid) {
        return dao.findById(uuid).map(mapper::toDomain);
    }

    @Override
    public Belt save(Belt belt) {
        var saved = dao.save(mapper.toEntity(belt));
        return mapper.toDomain(saved);
    }
}