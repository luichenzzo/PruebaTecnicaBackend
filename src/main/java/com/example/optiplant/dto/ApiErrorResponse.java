package com.example.optiplant.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error payload returned by REST exception handlers.
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> validationErrors
) {

    /**
     * Creates a non-validation error response using the current timestamp.
     *
     * @param status HTTP status code
     * @param error short error label
     * @param message human-readable error message
     * @return error response
     */
    public static ApiErrorResponse of(int status, String error, String message) {
        return new ApiErrorResponse(Instant.now(), status, error, message, null);
    }

    /**
     * Creates a validation error response with field-level errors.
     *
     * @param status HTTP status code
     * @param error short error label
     * @param validationErrors validation messages keyed by field name
     * @return validation error response
     */
    public static ApiErrorResponse validation(int status, String error, Map<String, String> validationErrors) {
        return new ApiErrorResponse(Instant.now(), status, error, "Validation failed", validationErrors);
    }
}
