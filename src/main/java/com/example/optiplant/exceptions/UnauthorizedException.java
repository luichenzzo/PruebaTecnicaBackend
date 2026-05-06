package com.example.optiplant.exceptions;

/**
 * Exception thrown when an operation requires an authenticated user.
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Creates an exception with a client-facing message.
     *
     * @param message reason authentication is required or invalid
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
