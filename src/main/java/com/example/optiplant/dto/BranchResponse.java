package com.example.optiplant.dto;

import com.example.optiplant.model.Branch;
import java.util.UUID;

public record BranchResponse(
        UUID id,
        String code,
        String name,
        String address,
        UUID createdById,
        UUID updatedById
) {

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

