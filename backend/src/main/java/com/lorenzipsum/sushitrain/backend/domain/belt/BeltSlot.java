package com.lorenzipsum.sushitrain.backend.domain.belt;

import com.lorenzipsum.sushitrain.backend.domain.plate.Plate;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "belt_slots")
@Getter
public class BeltSlot {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "belt_id")
    private Belt belt;

    private int positionIndex;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plate_id")
    private Plate plate;

    protected BeltSlot() {
    }

    private BeltSlot(UUID id, Belt belt, int positionIndex, Plate plate) {
        this.id = id;
        this.belt = belt;
        this.positionIndex = positionIndex;
        this.plate = plate;
    }

    public static BeltSlot emptyAt(Belt belt, int positionIndex) {
        return new BeltSlot(UUID.randomUUID(), belt, positionIndex, null);
    }

    public boolean isEmpty() {
        return plate == null;
    }

    public void place(Plate p) {
        if (p == null) {
            throw new IllegalArgumentException("Plate cannot be null");
        }
        if (this.plate != null) {
            throw new IllegalStateException("Plate is already assigned to belt slot");
        }
        this.plate = p;
    }

    public Plate take() {
        Plate p = this.plate;
        this.plate = null;
        return p;
    }

    public Plate takeStrict() {
        if (isEmpty()) throw new IllegalStateException("No plate to take");
        return take();
    }
}
