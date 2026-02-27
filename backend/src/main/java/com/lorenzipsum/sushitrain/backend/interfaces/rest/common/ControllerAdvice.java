package com.lorenzipsum.sushitrain.backend.interfaces.rest.common;

import com.lorenzipsum.sushitrain.backend.application.common.NotEnoughFreeSlotsException;
import com.lorenzipsum.sushitrain.backend.application.common.PlateNotPickableException;
import com.lorenzipsum.sushitrain.backend.application.common.ResourceNotFoundException;
import com.lorenzipsum.sushitrain.backend.application.common.SeatAlreadyOccupiedException;
import com.lorenzipsum.sushitrain.backend.application.common.SeatNotOccupiedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;

@RestControllerAdvice
@SuppressWarnings("unused")
public class ControllerAdvice {
    public static final String PROBLEM_BASE_URI = "https://api.sushitrain/errors";
    public static final String PROBLEM_404_TITLE = "Resource not found";
    public static final String PROBLEM_404_URI = PROBLEM_BASE_URI + "/not-found";
    public static final String PROBLEM_400_INVALID_PARAM_TITLE = "Invalid parameter";
    public static final String PROBLEM_400_INVALID_PARAM_URI = PROBLEM_BASE_URI + "/invalid-parameter";
    public static final String PROBLEM_400_MISSING_PARAM_TITLE = "Missing required parameter";
    public static final String PROBLEM_400_MISSING_PARAM_URI = PROBLEM_BASE_URI + "/missing-parameter";
    public static final String PROBLEM_400_VALIDATION_FAILED_TITLE = "Validation failed";
    public static final String PROBLEM_400_VALIDATION_FAILED_URI = PROBLEM_BASE_URI + "/validation-failed";
    public static final String PROBLEM_400_MALFORMED_REQUEST_TITLE = "Malformed request";
    public static final String PROBLEM_400_MALFORMED_REQUEST_URI = PROBLEM_BASE_URI + "/malformed-json";
    public static final String PROBLEM_500_TITLE = "Internal server error";
    public static final String PROBLEM_500_URI = PROBLEM_BASE_URI + "/internal";
    public static final String PROBLEM_409_TITLE = "Conflict";
    public static final String PROBLEM_409_URI = "https://lorenzipsum.com/problems/conflict";

    // 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle(PROBLEM_404_TITLE);
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create(PROBLEM_404_URI));
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }

    // 400: wrong UUID in path, wrong types in params, etc.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(PROBLEM_400_INVALID_PARAM_TITLE);
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "required type";
        pd.setDetail("Parameter '" + ex.getName() + "' must be a " + requiredType);
        pd.setType(URI.create(PROBLEM_400_INVALID_PARAM_URI));
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }

    // 400: missing required query params (?page=...&size=...)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingRequestParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(PROBLEM_400_MISSING_PARAM_TITLE);
        pd.setDetail("Missing required query parameter '" + ex.getParameterName() + "'");
        pd.setType(URI.create(PROBLEM_400_MISSING_PARAM_URI));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("parameter", ex.getParameterName());
        return pd;
    }

    // 400: method parameter validation (e.g. @Min/@Max on @RequestParam)
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(PROBLEM_400_VALIDATION_FAILED_TITLE);
        pd.setDetail("One or more parameters are invalid");
        pd.setType(URI.create(PROBLEM_400_VALIDATION_FAILED_URI));
        pd.setInstance(URI.create(request.getRequestURI()));

        var errors = ex.getConstraintViolations().stream()
                .collect(java.util.stream.Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage() != null ? v.getMessage() : "invalid",
                        (a, b) -> a
                ));
        pd.setProperty("errors", errors);
        return pd;
    }

    // 400: invalid JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(PROBLEM_400_MALFORMED_REQUEST_TITLE);
        pd.setDetail("Request body is missing or malformed");
        pd.setType(URI.create(PROBLEM_400_MALFORMED_REQUEST_URI));
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }

    // 400: bean validation failures (DTO @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(PROBLEM_400_VALIDATION_FAILED_TITLE);
        pd.setDetail("One or more fields are invalid");
        pd.setType(URI.create(PROBLEM_400_VALIDATION_FAILED_URI));
        pd.setInstance(URI.create(request.getRequestURI()));
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(java.util.stream.Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                        (a, b) -> a
                ));
        pd.setProperty("errors", errors);
        return pd;
    }

    // 400: validation failures on method parameters (e.g. @Min/@Max on @RequestParam in controller method)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleHandlerMethodValidation(HandlerMethodValidationException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(PROBLEM_400_VALIDATION_FAILED_TITLE);
        pd.setDetail("One or more parameters are invalid");
        pd.setType(URI.create(PROBLEM_400_VALIDATION_FAILED_URI));
        pd.setInstance(URI.create(request.getRequestURI()));

        var errors = ex.getAllErrors().stream()
                .map(e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "invalid")
                .distinct()
                .toList();

        pd.setProperty("errors", errors);
        return pd;
    }

    // 400: other invalid parameters (e.g. invalid enum value, negative number where only positive allowed, etc.)
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(PROBLEM_400_INVALID_PARAM_TITLE);
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create(PROBLEM_400_INVALID_PARAM_URI));
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }

    // 409: not enough free slots on the belt to place a new plate
    @ExceptionHandler(NotEnoughFreeSlotsException.class)
    public ResponseEntity<ProblemDetail> handleNotEnoughFreeSlots(NotEnoughFreeSlotsException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle(PROBLEM_409_TITLE);
        pd.setType(URI.create(PROBLEM_409_URI));
        pd.setDetail(ex.getMessage());
        pd.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON)
                .body(pd);
    }

    // 409: seat already occupied
    @ExceptionHandler(SeatAlreadyOccupiedException.class)
    public ResponseEntity<ProblemDetail> handleSeatOccupied(SeatAlreadyOccupiedException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle(PROBLEM_409_TITLE);
        pd.setType(URI.create(PROBLEM_409_URI));
        pd.setDetail(ex.getMessage());
        pd.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON)
                .body(pd);
    }

    // 409: seat is not occupied
    @ExceptionHandler(SeatNotOccupiedException.class)
    public ResponseEntity<ProblemDetail> handleSeatNotOccupied(SeatNotOccupiedException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle(PROBLEM_409_TITLE);
        pd.setType(URI.create(PROBLEM_409_URI));
        pd.setDetail(ex.getMessage());
        pd.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON)
                .body(pd);
    }

    // 409: plate cannot be picked in current state
    @ExceptionHandler(PlateNotPickableException.class)
    public ResponseEntity<ProblemDetail> handlePlateNotPickable(PlateNotPickableException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle(PROBLEM_409_TITLE);
        pd.setType(URI.create(PROBLEM_409_URI));
        pd.setDetail(ex.getMessage());
        pd.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON)
                .body(pd);
    }

    // 409: database-level uniqueness/conflict violations
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle(PROBLEM_409_TITLE);
        pd.setType(URI.create(PROBLEM_409_URI));
        pd.setDetail("Request conflicts with current resource state");
        pd.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON)
                .body(pd);
    }

    // 500
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle(PROBLEM_500_TITLE);
        pd.setDetail("Unexpected server error");
        pd.setType(URI.create(PROBLEM_500_URI));
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }
}
