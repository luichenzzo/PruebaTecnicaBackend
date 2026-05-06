package com.example.optiplant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating or updating a branch.
 */
public record BranchRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 500) String address
) {
}

