package com.lorenzipsum.sushitrain.backend.domain.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoneyYenTest {
    public static final MoneyYen ZERO = new MoneyYen(0);
    public static final MoneyYen THREE = new MoneyYen(3);
    public static final MoneyYen FIVE = new MoneyYen(5);

    @Test
    @DisplayName("MoneyYen can be instantiated but no negative amounts are allowed")
    void instantiate_ok() {
        assertAll("Assert instantiation",
                () -> assertEquals(5, FIVE.getAmount()),
                () -> assertEquals(0, ZERO.getAmount()),
                () -> assertThrows(IllegalArgumentException.class, () -> new MoneyYen(-4)));
    }

    @Test
    @DisplayName("Equals and hashCode are based on amount")
    void equals_hashcode() {
        var a = new MoneyYen(500);
        var b = new MoneyYen(500);
        var c = new MoneyYen(400);

        assertAll("Assert equality and hashCode consistency",
                () -> assertEquals(a, b),
                () -> assertNotEquals(a, c),
                () -> assertEquals(a.hashCode(), b.hashCode())
        );
    }


    @Test
    @DisplayName("equals is reflexive and transitive")
    @SuppressWarnings("AssertEqualsCalledOnItself")
    void equals_laws() {
        var a = new MoneyYen(700);
        var b = new MoneyYen(700);
        var c = new MoneyYen(700);
        assertAll("Assert equality",
                () -> assertEquals(a, a),
                () -> assertEquals(a, b),
                () -> assertEquals(b, c),
                () -> assertEquals(a, c)        // transitive
        );
    }

    @Test
    @DisplayName("Addition works fine")
    void plus_ok() {
        var eight = FIVE.plus(THREE);

        assertAll("Assert addition of amounts",
                () -> assertEquals(8, eight.getAmount()),
                () -> assertThrows(IllegalArgumentException.class, () -> FIVE.plus(null)));
    }

    @Test
    @DisplayName("Plus returns new instance and does not mutate operands")
    void plus_immutability() {
        var eight = FIVE.plus(THREE);
        assertNotSame(FIVE, eight);
        assertEquals(5, FIVE.getAmount());
        assertEquals(3, THREE.getAmount());
        assertEquals(8, eight.getAmount());
    }

    @Test
    @DisplayName("plus throws on integer overflow")
    void plus_overflow() {
        var big = new MoneyYen(Integer.MAX_VALUE);
        assertThrows(ArithmeticException.class, () -> big.plus(new MoneyYen(1)));
    }

    @Test
    @DisplayName("ToString method works correctly")
    void testToString() {
        var amountFiveHundred = new MoneyYen(500);
        var amountOverThousand = new MoneyYen(1250);
        var amountOverTenThousand = new MoneyYen(25432);

        assertEquals("￥5", FIVE.toString());
        assertEquals("￥500", amountFiveHundred.toString());
        assertEquals("￥1,250", amountOverThousand.toString());
        assertEquals("￥25,432", amountOverTenThousand.toString());
    }

    @Test
    @DisplayName("IsZero method works correctly")
    void isZero() {
        assertAll("Asserting isZero method",
                () -> assertTrue(new MoneyYen(0).isZero()),
                () -> assertFalse(new MoneyYen(1).isZero()),
                () -> assertFalse(new MoneyYen(3).isZero())
        );
    }

    @Test
    @DisplayName("Helper method of is working fine")
    void of() {
        assertAll("Asserting helper method of",
                () -> assertEquals(new MoneyYen(1), MoneyYen.of(1)),
                () -> assertEquals(new MoneyYen(199), MoneyYen.of(199))
        );
    }
}