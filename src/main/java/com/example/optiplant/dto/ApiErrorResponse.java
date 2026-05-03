package com.example.optiplant.dto;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> validationErrors
) {

    public static ApiErrorResponse of(int status, String error, String message) {
        return new ApiErrorResponse(Instant.now(), status, error, message, null);
    }

    public static ApiErrorResponse validation(int status, String error, Map<String, String> validationErrors) {
        return new ApiErrorResponse(Instant.now(), status, error, "Validation failed", validationErrors);
    }
}
