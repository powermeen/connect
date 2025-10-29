// src/main/java/com/cat/connect/common/GlobalExceptionHandler.java
package com.cat.connect.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badRequest(IllegalArgumentException e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> fkViolation(DataIntegrityViolationException e){
        return ResponseEntity.badRequest().body("Foreign key violation: check userId/roleId exist");
    }
}
