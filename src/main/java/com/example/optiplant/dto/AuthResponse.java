package com.example.optiplant.dto;

/**
 * Authentication response containing a bearer token and the authenticated user.
 */
public record AuthResponse(
        String token,
        String tokenType,
        UserResponse user
) {

    /**
     * Creates a bearer-token authentication response.
     *
     * @param token JWT access token
     * @param user authenticated user details
     */
    public AuthResponse(String token, UserResponse user) {
        this(token, "Bearer", user);
    }
}
