package com.panvel.mneubarth.albumratings.exceptions;

import com.mongodb.MongoWriteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ErrorResponse> handleEntityNotFound(EntityNotFound ex) {
        return Mono.just(new ErrorResponse("Entity Not Found", ex.getMessage()));
    }

    @ExceptionHandler(WebClientResponseException.NotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ErrorResponse> handleWebClientEntityNotFound(WebClientResponseException.NotFound ex) {
        return Mono.just(new ErrorResponse("Entity Not Found", "Could not get the requested entity from external service"));
    }

    @ExceptionHandler(WebClientResponseException.Unauthorized.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Mono<ErrorResponse> handleWebClientUnauthorized(WebClientResponseException.Unauthorized ex) {
        return Mono.just(new ErrorResponse("External Unauthorized", "External service returned 401 Unauthorized"));
    }

    @ExceptionHandler(TokenException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<ErrorResponse> handleTokenException(TokenException ex) {
        return Mono.just(new ErrorResponse("Token Error", ex.getMessage()));
    }

    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponse> handleAuthException(AuthException ex) {
        return Mono.just(new ErrorResponse("Authentication Error", ex.getMessage()));
    }

    @ExceptionHandler(MongoWriteException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleMongoWriteException(MongoWriteException ex) {
        if (ex.getError().getCode() == 11000) {
            ErrorResponse errorResponse = new ErrorResponse("Duplicate Key Error", "A record with the given unique field already exists.");
            return Mono.just(ResponseEntity.status(409).body(errorResponse));
        }
        ErrorResponse errorResponse = new ErrorResponse("Database Error", ex.getError().getMessage());
        return Mono.just(ResponseEntity.status(500).body(errorResponse));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return Mono.just(new ErrorResponse("Illegal Argument", ex.getMessage()));
    }

    public record ErrorResponse(String error, String message) {}
}
