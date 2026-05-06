package com.example.optiplant.exceptions;

/**
 * Exception thrown when a requested domain resource cannot be found.
 */
public class NotFoundException extends RuntimeException {

    /**
     * Creates an exception with a client-facing message.
     *
     * @param message missing resource description
     */
    public NotFoundException(String message) {
        super(message);
    }
}
