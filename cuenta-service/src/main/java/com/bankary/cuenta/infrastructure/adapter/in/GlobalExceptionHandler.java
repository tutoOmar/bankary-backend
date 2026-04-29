package com.bankary.cuenta.infrastructure.adapter.in;

import com.bankary.cuenta.application.exception.ConflictException;
import com.bankary.cuenta.application.exception.ResourceNotFoundException;
import com.bankary.cuenta.application.exception.SaldoInsuficienteException;
import com.bankary.cuenta.domain.exception.CuentaDuplicadaException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Recurso no encontrado | método={} uri={} | mensaje={}", 
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleSaldoInsuficiente(SaldoInsuficienteException ex, HttpServletRequest request) {
        log.error("Saldo insuficiente | método={} uri={} | mensaje={}", 
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, HttpServletRequest request) {
        log.error("Conflicto de datos | método={} uri={} | mensaje={}", 
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CuentaDuplicadaException.class)
    public ResponseEntity<ErrorResponse> handleCuentaDuplicada(CuentaDuplicadaException ex, HttpServletRequest request) {
        log.error("Cuenta duplicada | método={} uri={} | mensaje={}", 
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Error de validación | método={} uri={} | detalles=[{}]", 
                request.getMethod(), request.getRequestURI(), details);
        return buildErrorResponse("Error de validación: " + details, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado interno | método={} uri={} | excepción={} | mensaje={}", 
                request.getMethod(), request.getRequestURI(), ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return buildErrorResponse("Ha ocurrido un error inesperado en el servidor", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status) {
        ErrorResponse error = new ErrorResponse(LocalDateTime.now(), message, status.value());
        return new ResponseEntity<>(error, status);
    }

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private String message;
        private int status;
    }
}
