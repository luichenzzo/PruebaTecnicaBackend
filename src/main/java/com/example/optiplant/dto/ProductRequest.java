package com.example.optiplant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(
        @NotBlank @Size(max = 100) String sku,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 1000) String description,
        UUID unitOfMeasureId,
        UUID supplierId,
        @PositiveOrZero BigDecimal defaultCost
) {
}
