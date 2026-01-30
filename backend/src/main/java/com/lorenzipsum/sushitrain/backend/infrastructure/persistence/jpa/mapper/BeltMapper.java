package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.mapper;

import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.entity.BeltEntity;
import org.springframework.stereotype.Component;

@Component
public class BeltMapper {

    private final BeltSlotMapper slotMapper;
    private final SeatMapper seatMapper;

    public BeltMapper(BeltSlotMapper slotMapper, SeatMapper seatMapper) {
        this.slotMapper = slotMapper;
        this.seatMapper = seatMapper;
    }

    public Belt toDomain(BeltEntity e) {
        if (e == null) return null;

        var slots = e.getSlots().stream()
                .map(slotMapper::toDomain)
                .toList();

        var seats = e.getSeats().stream()
                .map(seatMapper::toDomain)
                .toList();

        return Belt.rehydrate(
                e.getId(),
                e.getName(),
                e.getSlotCount(),
                e.getBaseRotationOffset(),
                e.getTickIntervalMs(),
                e.getSpeedSlotsPerTick(),
                slots,
                seats,
                e.getOffsetStartedAt()
        );
    }

    public BeltEntity toEntity(Belt d) {
        if (d == null) return null;
        return new BeltEntity(
                d.getId(),
                d.getName(),
                d.getSlotCount(),
                d.getBaseRotationOffset(),
                d.getOffsetStartedAt(),
                d.getTickIntervalMs(),
                d.getSpeedSlotsPerTick()
        );
    }
}
