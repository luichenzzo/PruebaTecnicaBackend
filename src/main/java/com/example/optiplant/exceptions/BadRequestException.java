package com.example.optiplant.exceptions;

/**
 * Exception thrown when a request violates business validation rules.
 */
public class BadRequestException extends RuntimeException {

    /**
     * Creates an exception with a client-facing message.
     *
     * @param message reason the request is invalid
     */
    public BadRequestException(String message) {
        super(message);
    }
}
