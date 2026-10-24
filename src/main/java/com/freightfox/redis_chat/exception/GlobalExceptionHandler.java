package com.freightfox.redis_chat.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        // We need to determine which response type to use based on the request.
        // Since we don't have the request context here easily, we can use a generic map
        // or check the endpoint. However, the assignment uses specific Response DTOs.
        // To be minimal and consistent, we can return a map that matches the DTO structure.

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
            "message", errorMessage,
            "status", "error"
        ));
    }
}
