package com.example.optiplant.dto;

import com.example.optiplant.model.Product;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        UUID unitOfMeasureId,
        UUID supplierId,
        BigDecimal defaultCost,
        UUID createdById,
        UUID updatedById
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getUnitOfMeasure() == null ? null : product.getUnitOfMeasure().getId(),
                product.getSupplier() == null ? null : product.getSupplier().getId(),
                product.getDefaultCost(),
                product.getCreatedBy() == null ? null : product.getCreatedBy().getId(),
                product.getUpdatedBy() == null ? null : product.getUpdatedBy().getId()
        );
    }
}
