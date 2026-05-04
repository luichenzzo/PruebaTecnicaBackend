package com.example.optiplant.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InventorySummaryResponse(
        UUID productId,
        String productSku,
        String productName,
        BigDecimal totalQuantity,
        BigDecimal totalReserved
) {
}
