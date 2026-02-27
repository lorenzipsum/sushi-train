package com.lorenzipsum.sushitrain.backend.interfaces.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MaxNewPlatesPerRequestValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface MaxNewPlatesPerRequest {

    String message() default "numOfPlates must be <= configured maximum";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}