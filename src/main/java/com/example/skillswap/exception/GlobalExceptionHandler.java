package com.example.skillswap.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InsufficientCreditsException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientCredits(
            InsufficientCreditsException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "Insufficient Credits", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidSessionStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidState(
            InvalidSessionStateException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "Invalid Session State", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedProviderException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(
            UnauthorizedProviderException ex, HttpServletRequest request) {
        return buildError(HttpStatus.FORBIDDEN, "Unauthorized", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOperation(
            InvalidOperationException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "Invalid Operation", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = collectValidationMessages(ex);
        return buildError(HttpStatus.BAD_REQUEST, "Validation Failed", message, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                ex.getMessage(), request.getRequestURI());
    }

    private String collectValidationMessages(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
    }

    private ResponseEntity<Map<String, Object>> buildError(
            HttpStatus status, String error, String message, String path) {
        Map<String, Object> body = buildErrorBody(status, error, message, path);
        return ResponseEntity.status(status).body(body);
    }

    private Map<String, Object> buildErrorBody(HttpStatus status, String error, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);
        return body;
    }
}
