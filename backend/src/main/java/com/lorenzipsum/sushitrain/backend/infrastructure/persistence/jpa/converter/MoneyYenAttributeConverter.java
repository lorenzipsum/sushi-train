package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.converter;

import com.lorenzipsum.sushitrain.backend.domain.common.MoneyYen;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MoneyYenAttributeConverter implements AttributeConverter<MoneyYen, Integer> {

    @Override
    public Integer convertToDatabaseColumn(MoneyYen attribute) {
        return (attribute == null) ? null : attribute.amount();
    }

    @Override
    public MoneyYen convertToEntityAttribute(Integer dbData) {
        return (dbData == null) ? null : new MoneyYen(dbData);
    }
}
