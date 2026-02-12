package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BeltJpaDao extends JpaRepository<BeltEntity, UUID> {
}
