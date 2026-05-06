package com.example.optiplant.exceptions;

import com.example.optiplant.dto.ApiErrorResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converts validation, authentication, authorization, and domain exceptions into
 * standard API error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles bean validation errors from request payloads.
     *
     * @param exception validation exception raised by Spring MVC
     * @return structured validation error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ApiErrorResponse.validation(HttpStatus.BAD_REQUEST.value(), "Bad Request", errors);
    }

    /**
     * Handles invalid business requests.
     *
     * @param exception request exception
     * @return structured bad-request response
     */
    @ExceptionHandler({IllegalArgumentException.class, BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleBadRequest(RuntimeException exception) {
        return ApiErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Bad Request", exception.getMessage());
    }

    /**
     * Handles missing domain resources.
     *
     * @param exception missing-resource exception
     * @return structured not-found response
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNotFound(NotFoundException exception) {
        return ApiErrorResponse.of(HttpStatus.NOT_FOUND.value(), "Not Found", exception.getMessage());
    }

    /**
     * Handles failed username/password authentication.
     *
     * @return structured unauthorized response
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleBadCredentials() {
        return ApiErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", "Invalid credentials");
    }

    /**
     * Handles unauthenticated access to protected operations.
     *
     * @param exception unauthorized exception
     * @return structured unauthorized response
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleUnauthorized(UnauthorizedException exception) {
        return ApiErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", exception.getMessage());
    }

    /**
     * Handles authenticated requests that fail authorization.
     *
     * @param exception forbidden exception
     * @return structured forbidden response
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorResponse handleForbidden(ForbiddenException exception) {
        return ApiErrorResponse.of(HttpStatus.FORBIDDEN.value(), "Forbidden", exception.getMessage());
    }
}
