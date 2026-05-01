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

import java.util.List;

/**
 * Global exception interceptor.
 * Ensures internal errors do not reach Spring Security (preventing 403 Forbidden)
 * and formats error messages in a user-friendly way for the Frontend.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captures @Valid errors thrown in DTOs (Controller level).
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
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<List<ValidationErrorData>> handleConstraintViolation(ConstraintViolationException ex) {
        var errors = ex.getConstraintViolations().stream()
                .map(ValidationErrorData::new)
                .toList();

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Captures transaction errors. Hibernate usually wraps ConstraintViolationException
     * inside a TransactionSystemException during flush/commit.
     */
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<?> handleTransactionSystemException(TransactionSystemException ex) {
        // Unwraps the error to check if the root cause is a validation failure (e.g., invalid CNPJ)
        if (ex.getRootCause() instanceof ConstraintViolationException constraintException) {
            return handleConstraintViolation(constraintException);
        }

        // If it is another database error, returns a generic 500 to prevent architecture leakage
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal error during transaction processing.");
    }

    /**
     * Internal record to format the error response JSON.
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