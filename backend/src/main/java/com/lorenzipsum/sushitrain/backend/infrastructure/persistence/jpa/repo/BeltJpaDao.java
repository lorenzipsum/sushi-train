package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BeltJpaDao extends JpaRepository<BeltEntity, UUID> {
    @EntityGraph(attributePaths = {"slots", "slots.plate", "seats"})
    Optional<BeltEntity> findWithSlotsAndSeatsById(UUID id);
}
