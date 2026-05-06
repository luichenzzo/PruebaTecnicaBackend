package com.example.optiplant.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Shared line-item request for sales, transfers, and purchase orders.
 */
public record OrderItemRequest(
        @NotNull UUID productId,
        @NotNull @Positive BigDecimal quantity,
        @PositiveOrZero BigDecimal unitPrice
) {
}
