package com.lorenzipsum.sushitrain.backend.interfaces.rest.common;

import com.lorenzipsum.sushitrain.backend.application.plate.PlateNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(PlateNotFoundException.class)
    @SuppressWarnings("unused")
    public ProblemDetail handlePlateNotFound(PlateNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Plate Not Found");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }
}
