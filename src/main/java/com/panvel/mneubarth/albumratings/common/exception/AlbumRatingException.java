package com.panvel.mneubarth.albumratings.common.exception;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
@Builder
public class AlbumRatingException extends RuntimeException {

    private final HttpStatusCode statusCode;
    private final String message;

    public AlbumRatingException(HttpStatusCode statusCode, String message) {
        super(message);
        this.message = message;
        this.statusCode = statusCode;
    }

}
