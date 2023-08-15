package com.jolly.microservices.api.exceptions;

/**
 * @author jolly
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException() {}

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadRequestException(Throwable cause) {
        super(cause);
    }
}
