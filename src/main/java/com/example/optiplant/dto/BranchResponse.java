package com.example.optiplant.dto;

import com.example.optiplant.model.Branch;
import java.util.UUID;

/**
 * Response payload that exposes branch details and audit identifiers.
 */
public record BranchResponse(
        UUID id,
        String code,
        String name,
        String address,
        UUID createdById,
        UUID updatedById
) {

    /**
     * Maps a branch entity into its API response form.
     *
     * @param branch source branch entity
     * @return branch response
     */
    public static BranchResponse from(Branch branch) {
        return new BranchResponse(
                branch.getId(),
                branch.getCode(),
                branch.getName(),
                branch.getAddress(),
                branch.getCreatedBy() == null ? null : branch.getCreatedBy().getId(),
                branch.getUpdatedBy() == null ? null : branch.getUpdatedBy().getId()
        );
    }
}

