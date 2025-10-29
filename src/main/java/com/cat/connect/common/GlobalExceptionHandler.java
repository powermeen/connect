// src/main/java/com/cat/connect/common/GlobalExceptionHandler.java
package com.cat.connect.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException; // ✅ use validation BindException
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ Single handler for IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex,
                                                          HttpServletRequest req) {
        ApiError body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ✅ Validation errors (@Valid on request bodies/params)
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiError> handleValidation(Exception ex, HttpServletRequest req) {
        ApiError body = ApiError.of(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Validation Failed",
                ex.getMessage(),
                req.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    // ✅ FK/unique constraint violations, etc.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex,
                                                        HttpServletRequest req) {
        String message = friendlySqlMessage(ex);
        ApiError body = ApiError.of(
                HttpStatus.CONFLICT.value(),
                "Data Integrity Violation",
                message,
                req.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // ✅ Fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex, HttpServletRequest req) {
        ApiError body = ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                req.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // --- helpers ---
    private String friendlySqlMessage(Throwable ex) {
        // Try to surface useful details without leaking raw SQL
        String msg = ex.getMessage();
        if (msg == null) return "Constraint violation";
        // Common MySQL hints (unique/fk)
        if (msg.contains("Duplicate entry")) return "Duplicate value violates a unique constraint";
        if (msg.contains("foreign key constraint fails") || msg.contains("Cannot add or update a child row")) {
            return "Foreign key violation: referenced record not found / in use";
        }
        return "Constraint violation";
    }
}
