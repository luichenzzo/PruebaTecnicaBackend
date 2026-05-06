package com.example.optiplant.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login payload accepted by the authentication endpoint.
 */
public record LoginRequest(
        @NotBlank String usernameOrEmail,
        @NotBlank String password
) {
}
