package com.example.optiplant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/**
 * Request payload for creating a sale.
 */
public record SaleRequest(
        @Size(max = 100) String saleNumber,
        @NotNull UUID branchId,
        @NotEmpty List<@Valid OrderItemRequest> items
) {
}
