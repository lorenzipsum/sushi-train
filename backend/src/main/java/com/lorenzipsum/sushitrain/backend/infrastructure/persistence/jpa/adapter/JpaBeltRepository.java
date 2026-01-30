package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.adapter;

import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.domain.belt.BeltRepository;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltSlotEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.PlateEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.SeatEntity;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.BeltSlotMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper.SeatMapper;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltJpaDao;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class JpaBeltRepository implements BeltRepository {

    private final BeltJpaDao dao;
    private final BeltMapper mapper;
    private final BeltSlotMapper slotMapper;
    private final SeatMapper seatMapper;
    private final EntityManager em;

    public JpaBeltRepository(BeltJpaDao dao, BeltMapper mapper, BeltSlotMapper slotMapper, SeatMapper seatMapper, EntityManager em) {
        this.dao = dao;
        this.mapper = mapper;
        this.slotMapper = slotMapper;
        this.seatMapper = seatMapper;
        this.em = em;
    }

    @Override
    public Optional<Belt> findById(UUID id) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");
        return dao.findWithSlotsAndSeatsById(id).map(mapper::toDomain);
    }

    @Override
    public Belt save(Belt belt) {
        if (belt == null) throw new IllegalArgumentException("Belt cannot be null");

        var entity = dao.findWithSlotsAndSeatsById(belt.getId())
                .orElseGet(() -> new BeltEntity(
                        belt.getId(),
                        belt.getName(),
                        belt.getSlotCount(),
                        belt.getBaseRotationOffset(),
                        belt.getOffsetStartedAt(),
                        belt.getTickIntervalMs(),
                        belt.getSpeedSlotsPerTick()
                ));

        entity.setBaseRotationOffset(belt.getBaseRotationOffset());
        entity.setOffsetStartedAt(belt.getOffsetStartedAt());
        entity.setTickIntervalMs(belt.getTickIntervalMs());
        entity.setSpeedSlotsPerTick(belt.getSpeedSlotsPerTick());

        // Seats: only initialize once (static config)
        if (entity.getSeats() == null || entity.getSeats().isEmpty()) {
            var newSeats = belt.getSeats().stream()
                    .map(s -> seatMapper.toEntity(s, entity))
                    .toList();
            entity.replaceSeats(newSeats);
        } else {
            assertSeatsMatchOrThrow(belt, entity);
        }

        // Slots: your existing logic (init once, otherwise update plate refs)
        if (entity.getSlots() == null || entity.getSlots().isEmpty()) {
            var newSlots = belt.getSlots().stream()
                    .map(s -> {
                        PlateEntity plateRef = (s.getPlateId() == null) ? null :
                                em.getReference(PlateEntity.class, s.getPlateId());
                        return slotMapper.toEntity(s, entity, plateRef);
                    })
                    .toList();

            entity.replaceSlots(newSlots);
        } else {
            assertSlotsTopologyMatchesOrThrow(belt, entity);
            var byPos = entity.getSlots().stream()
                    .collect(Collectors.toMap(
                            BeltSlotEntity::getPositionIndex,
                            s -> s
                    ));

            for (var domainSlot : belt.getSlots()) {
                var slotEntity = byPos.get(domainSlot.getPositionIndex());
                if (slotEntity == null) {
                    throw new IllegalStateException("Missing slot entity for position " + domainSlot.getPositionIndex());
                }
                PlateEntity plateRef = (domainSlot.getPlateId() == null)
                        ? null
                        : em.getReference(PlateEntity.class, domainSlot.getPlateId());
                slotEntity.setPlate(plateRef);
            }
        }

        if (entity.getSlotCount() != belt.getSlotCount()) {
            throw new IllegalStateException("Belt slotCount mismatch for belt " + belt.getId()
                    + ": DB=" + entity.getSlotCount() + ", domain=" + belt.getSlotCount());
        }

        var saved = dao.save(entity);
        return mapper.toDomain(saved);
    }

    private void assertSeatsMatchOrThrow(Belt belt, BeltEntity entity) {
        var dbSeats = entity.getSeats();
        var domainSeats = belt.getSeats();

        if (dbSeats == null) dbSeats = List.of();
        if (domainSeats == null) domainSeats = List.of();

        if (dbSeats.size() != domainSeats.size()) {
            throw new IllegalStateException("Seat config mismatch for belt " + belt.getId()
                    + ": DB has " + dbSeats.size() + " seats, domain has " + domainSeats.size());
        }

        // Map DB seats by id for direct comparison
        var dbById = dbSeats.stream().collect(Collectors.toMap(
                SeatEntity::getId,
                s -> s
        ));

        for (var s : domainSeats) {
            var db = dbById.get(s.getId());
            if (db == null) {
                throw new IllegalStateException("Seat config mismatch for belt " + belt.getId()
                        + ": seat id not found in DB: " + s.getId()
                        + " (label=" + s.getLabel() + ", pos=" + s.getPositionIndex() + ")");
            }

            if (!Objects.equals(db.getLabel(), s.getLabel())) {
                throw new IllegalStateException("Seat config mismatch for belt " + belt.getId()
                        + ": label differs for seat " + s.getId()
                        + " (DB=" + db.getLabel() + ", domain=" + s.getLabel() + ")");
            }

            if (db.getPositionIndex() != s.getPositionIndex()) {
                throw new IllegalStateException("Seat config mismatch for belt " + belt.getId()
                        + ": position differs for seat " + s.getId()
                        + " (DB=" + db.getPositionIndex() + ", domain=" + s.getPositionIndex() + ")");
            }
        }
    }

    private void assertSlotsTopologyMatchesOrThrow(Belt belt, BeltEntity entity) {
        var dbSlots = entity.getSlots();
        var domainSlots = belt.getSlots();

        if (dbSlots == null) dbSlots = java.util.List.of();
        if (domainSlots == null) domainSlots = java.util.List.of();

        if (dbSlots.size() != domainSlots.size()) {
            throw new IllegalStateException("Belt slot topology mismatch for belt " + belt.getId()
                    + ": DB has " + dbSlots.size() + " slots, domain has " + domainSlots.size());
        }

        // Compare by id to ensure stable identity
        var dbById = dbSlots.stream().collect(java.util.stream.Collectors.toMap(
                BeltSlotEntity::getId,
                s -> s
        ));

        for (var s : domainSlots) {
            var db = dbById.get(s.getId());
            if (db == null) {
                throw new IllegalStateException("Belt slot topology mismatch for belt " + belt.getId()
                        + ": slot id not found in DB: " + s.getId()
                        + " (pos=" + s.getPositionIndex() + ")");
            }

            if (db.getPositionIndex() != s.getPositionIndex()) {
                throw new IllegalStateException("Belt slot topology mismatch for belt " + belt.getId()
                        + ": position differs for slot " + s.getId()
                        + " (DB=" + db.getPositionIndex() + ", domain=" + s.getPositionIndex() + ")");
            }
        }

        int slotCount = belt.getSlotCount();
        var dbPositions = dbSlots.stream().map(BeltSlotEntity::getPositionIndex).collect(Collectors.toSet());
        for (int i = 0; i < slotCount; i++) {
            if (!dbPositions.contains(i)) {
                throw new IllegalStateException("Belt slot topology mismatch for belt " + belt.getId()
                        + ": DB missing slot position " + i + " (expected 0.." + (slotCount - 1) + ")");
            }
        }
    }
}