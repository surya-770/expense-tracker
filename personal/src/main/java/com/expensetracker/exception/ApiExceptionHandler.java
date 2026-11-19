package com.expensetracker.exception;

import java.time.Instant;
import java.time.DateTimeException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> invalidRequest(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream().findFirst().map(e -> e.getDefaultMessage()).orElse("Request validation failed");
        return response(HttpStatus.BAD_REQUEST, message);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, Object>> invalidArgument(IllegalArgumentException ex) { return response(HttpStatus.BAD_REQUEST, ex.getMessage()); }
    @ExceptionHandler(ConflictException.class)
    ResponseEntity<Map<String,Object>> conflict(ConflictException ex){return response(HttpStatus.CONFLICT,ex.getMessage());}
    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<Map<String, Object>> unauthorized(BadCredentialsException ex) { return response(HttpStatus.UNAUTHORIZED, "Invalid email or password"); }
    @ExceptionHandler(DateTimeException.class)
    ResponseEntity<Map<String,Object>> invalidDate(DateTimeException ex){return response(HttpStatus.BAD_REQUEST,"Invalid date or reporting period");}
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String,Object>> conflict(DataIntegrityViolationException ex){return response(HttpStatus.CONFLICT,"The change conflicts with an existing record or database constraint");}
    private ResponseEntity<Map<String, Object>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("status", status.value(), "error", status.getReasonPhrase(), "message", message, "timestamp", Instant.now().toString()));
    }
}
