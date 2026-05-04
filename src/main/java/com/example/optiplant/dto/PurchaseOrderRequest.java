package com.example.optiplant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderRequest(
        @Size(max = 100) String orderNumber,
        @NotNull UUID supplierId,
        @NotNull UUID branchId,
        @NotEmpty List<@Valid OrderItemRequest> items
) {
}
