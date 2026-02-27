package com.lorenzipsum.sushitrain.backend.domain.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YenAmountTest {
    private final YenAmount ZERO = YenAmount.of(0);
    private final YenAmount THREE = YenAmount.of(3);
    private final YenAmount FIVE = YenAmount.of(5);

    @Test
    @DisplayName("YenAmount can be instantiated but no negative amounts are allowed")
    void instantiate_ok() {
        assertAll("Assert instantiation",
                () -> assertEquals(5, FIVE.amount()),
                () -> assertEquals(0, ZERO.amount()),
                () -> assertThrows(IllegalArgumentException.class, () -> YenAmount.of(-4)));
    }

    @Test
    @DisplayName("Equals and hashCode are based on amount")
    void equals_hashcode() {
        var a = YenAmount.of(500);
        var b = YenAmount.of(500);
        var c = YenAmount.of(400);

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
        var a = YenAmount.of(700);
        var b = YenAmount.of(700);
        var c = YenAmount.of(700);
        assertAll("Assert equality",
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
                () -> assertEquals(8, eight.amount()),
                () -> assertThrows(IllegalArgumentException.class, () -> FIVE.plus(null)));
    }

    @Test
    @DisplayName("Plus returns new instance and does not mutate operands")
    void plus_immutability() {
        var eight = FIVE.plus(THREE);
        assertNotSame(FIVE, eight);
        assertEquals(5, FIVE.amount());
        assertEquals(3, THREE.amount());
        assertEquals(8, eight.amount());
    }

    @Test
    @DisplayName("plus throws on integer overflow")
    void plus_overflow() {
        var big = YenAmount.of(Integer.MAX_VALUE);
        assertThrows(ArithmeticException.class, () -> big.plus(YenAmount.of(1)));
    }

    @Test
    @DisplayName("ToString method works correctly")
    void testToString() {
        var amountFiveHundred = YenAmount.of(500);
        var amountOverThousand = YenAmount.of(1250);
        var amountOverTenThousand = YenAmount.of(25432);

        assertEquals("￥5", FIVE.toString());
        assertEquals("￥500", amountFiveHundred.toString());
        assertEquals("￥1,250", amountOverThousand.toString());
        assertEquals("￥25,432", amountOverTenThousand.toString());
    }

    @Test
    @DisplayName("IsZero method works correctly")
    void isZero() {
        assertAll("Asserting isZero method",
                () -> assertTrue(YenAmount.of(0).isZero()),
                () -> assertFalse(YenAmount.of(1).isZero()),
                () -> assertFalse(YenAmount.of(3).isZero())
        );
    }

    @Test
    @DisplayName("Helper method of is working fine")
    void of() {
        assertAll("Asserting helper method of",
                () -> assertEquals(YenAmount.of(1), YenAmount.of(1)),
                () -> assertEquals(YenAmount.of(199), YenAmount.of(199))
        );
    }
}