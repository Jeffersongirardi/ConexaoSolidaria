package com.conexoessolidarias.api;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.conexoessolidarias.api")
public class ApiExceptionHandler {

    public record ErrorResponse(LocalDateTime timestamp, int status, String error,
                                String message, Map<String, String> errors) {
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,
                                                Map<String, String> errors) {
        return ResponseEntity.status(status).body(new ErrorResponse(LocalDateTime.now(),
                status.value(), status.getReasonPhrase(), message, errors));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        f -> f.getDefaultMessage() != null ? f.getDefaultMessage() : "inválido",
                        (a, b) -> a));
        return build(HttpStatus.BAD_REQUEST, "Dados inválidos", errors);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(EntityNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND,
                ex.getMessage() != null ? ex.getMessage() : "Recurso não encontrado", null);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> forbidden(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "Acesso negado", null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST,
                ex.getMessage() != null ? ex.getMessage() : "Requisição inválida", null);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> conflict(IllegalStateException ex) {
        return build(HttpStatus.CONFLICT,
                ex.getMessage() != null ? ex.getMessage() : "Conflito", null);
    }
}
