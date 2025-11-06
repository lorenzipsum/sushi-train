package com.lorenzipsum.sushitrain.backend.domain.common;


import jakarta.persistence.Embeddable;
import lombok.Getter;

/**
 * Integer Yen to avoid floating point.
 */
@Embeddable
@Getter
public class MoneyYen {
    private int amount;

    protected MoneyYen() {
    }

    public MoneyYen(int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount < 0");
        this.amount = amount;
    }

    public MoneyYen plus(MoneyYen other) {
        return new MoneyYen(this.amount + other.amount);
    }

    @Override
    public String toString() {
        return "¥" + amount;
    }
}
