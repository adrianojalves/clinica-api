package br.com.ajasoftware.clinica.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captures @Valid errors thrown in DTOs (Controller level).
     * Retorna um Array (Padrão esperado pelo Angular para HTTP 400).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ValidationErrorData>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var errors = ex.getFieldErrors().stream()
                .map(ValidationErrorData::new)
                .toList();

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Captures Entity validation errors (JPA/Hibernate level).
     * Retorna um Array (Padrão esperado pelo Angular para HTTP 400).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<List<ValidationErrorData>> handleConstraintViolation(ConstraintViolationException ex) {
        var errors = ex.getConstraintViolations().stream()
                .map(ValidationErrorData::new)
                .toList();

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Captures custom Business Rules validations.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<List<ValidationErrorData>> handleBusinessException(BusinessException ex) {
        ValidationErrorData error = new ValidationErrorData("global", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(List.of(error));
    }

    /**
     * Captures transaction errors.
     */
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<?> handleTransactionSystemException(TransactionSystemException ex) {
        if (ex.getRootCause() instanceof ConstraintViolationException constraintException) {
            return handleConstraintViolation(constraintException);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Internal error during transaction processing."));
    }

    /**
     * Captures ResponseStatusException (ex: 404 NOT FOUND, 409 CONFLICT).
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, String> errorBody = Map.of("message", ex.getReason() != null ? ex.getReason() : "Erro na requisição.");

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(errorBody);
    }

    /**
     * Internal record to format the error response JSON array.
     */
    private record ValidationErrorData(String field, String message) {

        // Constructor for DTO errors
        public ValidationErrorData(FieldError error) {
            this(error.getField(), error.getDefaultMessage());
        }

        // Constructor for JPA Entity errors
        public ValidationErrorData(ConstraintViolation<?> violation) {
            this(violation.getPropertyPath().toString(), violation.getMessage());
        }
    }
}