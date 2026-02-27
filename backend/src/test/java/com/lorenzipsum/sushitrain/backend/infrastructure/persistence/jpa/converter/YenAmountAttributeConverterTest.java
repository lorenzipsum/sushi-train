package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.converter;

import com.lorenzipsum.sushitrain.backend.domain.common.YenAmount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YenAmountAttributeConverterTest {

    private final YenAmountAttributeConverter converter = new YenAmountAttributeConverter();

    @Test
    @DisplayName("convert YenAmount to database column (Integer)")
    void convertToDatabaseColumn() {
        // given
        YenAmount money = new YenAmount(1234);

        // when
        Integer dbValue = converter.convertToDatabaseColumn(money);

        // then
        assertNotNull(dbValue);
        assertEquals(1234, dbValue);
    }

    @Test
    @DisplayName("convert null YenAmount to database column")
    void convertToDatabaseColumnNull() {
        // when
        Integer dbValue = converter.convertToDatabaseColumn(null);

        // then
        assertNull(dbValue);
    }

    @Test
    @DisplayName("convert Integer from database to YenAmount")
    void convertToEntityAttribute() {
        // when
        YenAmount money = converter.convertToEntityAttribute(5678);

        // then
        assertNotNull(money);
        assertEquals(5678, money.amount());
    }

    @Test
    @DisplayName("convert null Integer from database to YenAmount")
    void convertToEntityAttributeNull() {
        // when
        YenAmount money = converter.convertToEntityAttribute(null);

        // then
        assertNull(money);
    }
}
