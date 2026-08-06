package me.taubsie.dungeonhub.server.config;

import net.dungeonhub.exceptions.InvalidUpdateException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(InvalidUpdateException.class)
    public ResponseEntity<String> handleInvalidUpdate(InvalidUpdateException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}