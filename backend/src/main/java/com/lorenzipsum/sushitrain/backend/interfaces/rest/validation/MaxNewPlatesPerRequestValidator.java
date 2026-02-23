package com.lorenzipsum.sushitrain.backend.interfaces.rest.validation;

import com.lorenzipsum.sushitrain.backend.infrastructure.config.BeltPlacementProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class MaxNewPlatesPerRequestValidator implements ConstraintValidator<MaxNewPlatesPerRequest, Integer> {

    private final BeltPlacementProperties props;

    public MaxNewPlatesPerRequestValidator(BeltPlacementProperties props) {
        this.props = props;
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) return true; // @NotNull handles null if you want
        return value <= props.maxNewPlatesPerRequest();
    }
}