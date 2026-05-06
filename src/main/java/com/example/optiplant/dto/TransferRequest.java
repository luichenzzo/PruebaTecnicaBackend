package com.example.optiplant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/**
 * Request payload for creating branch-to-branch transfers.
 */
public record TransferRequest(
        @Size(max = 100) String transferNumber,
        @NotNull UUID fromBranchId,
        @NotNull UUID toBranchId,
        @NotEmpty List<@Valid OrderItemRequest> items
) {
}
