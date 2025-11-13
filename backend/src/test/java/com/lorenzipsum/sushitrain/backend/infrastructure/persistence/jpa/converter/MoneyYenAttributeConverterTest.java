package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.converter;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoneyYenAttributeConverterTest {

    private final MoneyYenAttributeConverter converter = new MoneyYenAttributeConverter();

    @Test
    @DisplayName("convert MoneyYen to database column (Integer)")
    void convertToDatabaseColumn() {
        // given
        MoneyYen money = new MoneyYen(1234);

        // when
        Integer dbValue = converter.convertToDatabaseColumn(money);

        // then
        assertNotNull(dbValue);
        assertEquals(1234, dbValue);
    }

    @Test
    @DisplayName("convert null MoneyYen to database column")
    void convertToDatabaseColumnNull() {
        // when
        Integer dbValue = converter.convertToDatabaseColumn(null);

        // then
        assertNull(dbValue);
    }

    @Test
    @DisplayName("convert Integer from database to MoneyYen")
    void convertToEntityAttribute() {
        // when
        MoneyYen money = converter.convertToEntityAttribute(5678);

        // then
        assertNotNull(money);
        assertEquals(5678, money.amount());
    }

    @Test
    @DisplayName("convert null Integer from database to MoneyYen")
    void convertToEntityAttributeNull() {
        // when
        MoneyYen money = converter.convertToEntityAttribute(null);

        // then
        assertNull(money);
    }
}
