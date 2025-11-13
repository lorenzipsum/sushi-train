package com.lorenzipsum.sushitrain.backend.domain.common;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Integer Yen to avoid floating point.
 */
public record MoneyYen(int amount) {

    public MoneyYen {
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be a negative value");
    }

    public MoneyYen plus(MoneyYen other) {
        if (other == null) throw new IllegalArgumentException("Amount cannot be null");
        long sum = (long) this.amount + (long) other.amount;
        if (sum > Integer.MAX_VALUE) throw new ArithmeticException("amount overflow");
        return new MoneyYen((int) sum);
    }

    public boolean isZero() {
        return amount == 0;
    }

    public static MoneyYen of(int amount) {
        return new MoneyYen(amount);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString() {
        return formatYen(amount);
    }

    /**
     * Formats an integer amount as Japanese Yen (¥),
     * with comma grouping and no decimals.
     *
     * @param amount the amount in yen (integer)
     * @return formatted string like "¥980" or "¥12,800"
     */
    private String formatYen(int amount) {
        var yenFormat = NumberFormat.getCurrencyInstance(Locale.JAPAN);
        yenFormat.setMaximumFractionDigits(0); // ensure no decimals
        return yenFormat.format(amount);
    }
}
