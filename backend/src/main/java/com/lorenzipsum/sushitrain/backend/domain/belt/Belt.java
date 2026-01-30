package com.lorenzipsum.sushitrain.backend.domain.belt;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;

@SuppressWarnings("LombokGetterMayBeUsed")
public class Belt {
    private static final int DEFAULT_TICK_INTERVAL_MS = 1000;
    private static final int DEFAULT_BASE_ROTATION_OFFSET = 0;
    private static final int DEFAULT_SPEED_SLOTS_PER_TICK = 1;

    private final UUID id;
    private final String name;
    private final int slotCount;

    private final List<BeltSlot> slots;
    private final List<Seat> seats;

    private int baseRotationOffset;
    private Instant offsetStartedAt;
    private int tickIntervalMs;
    private int speedSlotsPerTick;

    private Belt(UUID id,
                 String name,
                 int slotCount,
                 int baseRotationOffset,
                 int tickIntervalMs,
                 int speedSlotsPerTick,
                 List<BeltSlot> slots,
                 List<Seat> seats,
                 Instant offsetStartedAt) {

        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.slotCount = Math.max(1, slotCount);

        this.offsetStartedAt = offsetStartedAt == null ? Instant.EPOCH : offsetStartedAt;
        this.baseRotationOffset = normalizeOffset(baseRotationOffset, this.slotCount);
        this.tickIntervalMs = Math.max(1, tickIntervalMs);
        this.speedSlotsPerTick = Math.max(1, speedSlotsPerTick);

        this.slots = (slots == null) ? new ArrayList<>() : new ArrayList<>(slots);
        this.seats = (seats == null) ? new ArrayList<>() : new ArrayList<>(seats);
    }

    public static Belt create(String name, int slotCount, List<SeatSpec> seatSpecs) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (slotCount <= 0) throw new IllegalArgumentException("slotCount must be > 0");

        UUID id = UUID.randomUUID();

        List<BeltSlot> slots = IntStream.range(0, slotCount)
                .mapToObj(i -> BeltSlot.createEmptyAt(id, i))
                .toList();

        List<Seat> seats = createSeatsOrThrow(id, slotCount, seatSpecs);

        return new Belt(
                id,
                name.trim(),
                slotCount,
                DEFAULT_BASE_ROTATION_OFFSET,
                DEFAULT_TICK_INTERVAL_MS,
                DEFAULT_SPEED_SLOTS_PER_TICK,
                slots,
                seats,
                Instant.now()
        );
    }

    public static Belt rehydrate(UUID id,
                                 String name,
                                 int slotCount,
                                 int baseRotationOffset,
                                 int tickIntervalMs,
                                 int speedSlotsPerTick,
                                 List<BeltSlot> slots,
                                 List<Seat> seats,
                                 Instant offsetStartedAt) {

        return new Belt(id, name, slotCount, baseRotationOffset, tickIntervalMs, speedSlotsPerTick, slots, seats, offsetStartedAt);
    }

    private static List<Seat> createSeatsOrThrow(UUID beltId, int slotCount, List<SeatSpec> seatSpecs) {
        if (seatSpecs == null) return List.of();

        Set<String> labels = new HashSet<>();
        Set<Integer> positions = new HashSet<>();

        List<Seat> result = new ArrayList<>(seatSpecs.size());
        for (SeatSpec spec : seatSpecs) {
            if (spec == null) throw new IllegalArgumentException("SeatSpec must not be null");

            int pos = spec.positionIndex();
            if (pos < 0) throw new IllegalArgumentException("Seat position cannot be negative");
            if (pos >= slotCount)
                throw new IllegalArgumentException("Seat position must be < slotCount (" + slotCount + "), got: " + pos);

            Seat seat = Seat.createAt(beltId, spec.label(), pos);

            if (!labels.add(seat.getLabel())) {
                throw new IllegalArgumentException("Duplicate seat label within belt: " + seat.getLabel());
            }
            if (!positions.add(seat.getPositionIndex())) {
                throw new IllegalArgumentException("Duplicate seat position within belt: " + seat.getPositionIndex());
            }

            result.add(seat);
        }

        return result;
    }

    private static int normalizeOffset(int offset, int slotCount) {
        return ((offset % slotCount) + slotCount) % slotCount;
    }

    public int currentOffsetAt(Instant now) {
        if (now == null) throw new IllegalArgumentException("Timestamp 'now' cannot be null.");
        long elapsedMs = Duration.between(offsetStartedAt, now).toMillis();
        if (elapsedMs < 0) elapsedMs = 0;

        long elapsedTicks = elapsedMs / tickIntervalMs;
        long offset = baseRotationOffset + (elapsedTicks * (long) speedSlotsPerTick) % slotCount;
        return (int) offset;
    }

    public void rebaseOffsetAt(Instant now) {
        this.baseRotationOffset = currentOffsetAt(now);
        this.offsetStartedAt = now;
    }

    public void setSpeedSlotsPerTick(int speedSlotsPerTick, Instant now) {
        if (speedSlotsPerTick >= slotCount)
            throw new IllegalArgumentException("Speed slots per tick has to be smaller than amount of slots");
        if (now == null) throw new IllegalArgumentException("Timestamp 'now' cannot be null");

        rebaseOffsetAt(now);
        this.speedSlotsPerTick = Math.max(1, speedSlotsPerTick);
    }

    public void setTickIntervalMs(int tickIntervalMs, Instant now) {
        if (now == null) throw new IllegalArgumentException("Timestamp 'now' cannot be null");

        rebaseOffsetAt(now);
        this.tickIntervalMs = Math.max(1, tickIntervalMs);
    }

    @SuppressWarnings("unused")
    public Optional<Seat> findSeatById(UUID seatId) {
        if (seatId == null) return Optional.empty();
        return seats.stream().filter(s -> seatId.equals(s.getId())).findFirst();
    }

    @SuppressWarnings("unused")
    public Optional<Seat> findSeatByPosition(int positionIndex) {
        return seats.stream().filter(s -> s.getPositionIndex() == positionIndex).findFirst();
    }

    @SuppressWarnings("unused")
    public Optional<BeltSlot> findSlotByPosition(int positionIndex) {
        return slots.stream().filter(s -> s.getPositionIndex() == positionIndex).findFirst();
    }

    // getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSlotCount() {
        return slotCount;
    }

    public int getBaseRotationOffset() {
        return baseRotationOffset;
    }

    public Instant getOffsetStartedAt() {
        return offsetStartedAt;
    }

    public int getTickIntervalMs() {
        return tickIntervalMs;
    }

    public int getSpeedSlotsPerTick() {
        return speedSlotsPerTick;
    }

    public List<BeltSlot> getSlots() {
        return Collections.unmodifiableList(slots);
    }

    public List<Seat> getSeats() {
        return Collections.unmodifiableList(seats);
    }
}
