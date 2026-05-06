package com.example.optiplant.exceptions;

/**
 * Exception thrown when an authenticated user lacks permission for an action.
 */
public class ForbiddenException extends RuntimeException {

    /**
     * Creates an exception with a client-facing message.
     *
     * @param message reason the operation is forbidden
     */
    public ForbiddenException(String message) {
        super(message);
    }
}
