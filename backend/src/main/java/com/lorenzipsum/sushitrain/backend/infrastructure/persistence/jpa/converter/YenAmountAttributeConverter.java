package com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.converter;

import com.lorenzipsum.sushitrain.backend.domain.common.YenAmount;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class YenAmountAttributeConverter implements AttributeConverter<YenAmount, Integer> {

    @Override
    public Integer convertToDatabaseColumn(YenAmount attribute) {
        return (attribute == null) ? null : attribute.amount();
    }

    @Override
    public YenAmount convertToEntityAttribute(Integer dbData) {
        return (dbData == null) ? null : new YenAmount(dbData);
    }
}
