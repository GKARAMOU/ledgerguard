package dev.karamousalis.ledgerguard.shared;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ProblemResponse> notFound(ResourceNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ProblemResponse> domain(DomainException exception) {
        HttpStatus status = "CONCURRENT_UPDATE".equals(exception.getCode()) ? HttpStatus.CONFLICT : HttpStatus.UNPROCESSABLE_ENTITY;
        return response(status, exception.getCode(), exception.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemResponse> validation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "One or more fields are invalid.", errors);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ProblemResponse> authentication(AuthenticationException exception) {
        return response(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Username or password is incorrect.", Map.of());
    }

    private ResponseEntity<ProblemResponse> response(HttpStatus status, String code, String message, Map<String, String> validationErrors) {
        return ResponseEntity.status(status).body(new ProblemResponse(code, message, status.value(), Instant.now(), validationErrors));
    }
}
