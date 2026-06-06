package com.panvel.mneubarth.albumratings.common.exception;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(WebClientResponseException.Conflict.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<ErrorResponse> handleWebClientConflict(WebClientResponseException.Conflict ex) {
        return Mono.just(ErrorResponse.builder().error("Conflict").message("Conflict when creating new user").build());
    }

    @ExceptionHandler(WebClientResponseException.NotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ErrorResponse> handleWebClientConflict(WebClientResponseException.NotFound ex) {
        return Mono.just(ErrorResponse.builder().error("Not Found").message("Register not found").build());
    }

    @ExceptionHandler(AlbumRatingException.class)
    public ResponseEntity<ErrorResponse> handleHttpException(AlbumRatingException exception) {
        log.debug(
                "code={}, message={}",
                exception.getStatusCode(),
                exception.getMessage()
        );

        return ResponseEntity.status(exception.getStatusCode()).body(ErrorResponse.builder()
                .error(exception.getStatusCode().toString())
                .message(exception.getMessage())
                .build());
    }

    @Builder
    public record ErrorResponse(String error, String message) {
    }
}
