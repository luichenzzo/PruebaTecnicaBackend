package com.example.optiplant.dto;

import com.example.optiplant.model.PurchaseOrderItem;
import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderItemResponse(
        UUID productId,
        String productSku,
        String productName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {

    public static PurchaseOrderItemResponse from(PurchaseOrderItem item) {
        BigDecimal unitPrice = item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice();
        return new PurchaseOrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getSku(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                unitPrice.multiply(item.getQuantity())
        );
    }
}
