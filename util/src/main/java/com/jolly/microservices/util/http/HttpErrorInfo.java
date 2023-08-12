package com.jolly.microservices.util.http;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

/**
 * @author jolly
 */
public record HttpErrorInfo(
        ZonedDateTime timestamp,
        String path,
        HttpStatus httpStatus,
        String message
) {
    public HttpErrorInfo() {
        this(null, null, null, null);
    }

    public HttpErrorInfo(String path, HttpStatus httpStatus, String message) {
        this(ZonedDateTime.now(), path, httpStatus, message);
    }
}
