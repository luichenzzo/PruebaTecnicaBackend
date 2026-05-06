package com.example.optiplant.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request payload for setting a product's inventory quantity at a branch.
 */
public record InventoryAdjustmentRequest(
        @NotNull UUID productId,
        @NotNull UUID branchId,
        @NotNull @PositiveOrZero BigDecimal quantity,
        @Size(max = 1000) String notes
) {
}
