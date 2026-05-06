package com.example.optiplant.dto;

import com.example.optiplant.model.SaleItem;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response payload for a sale line item.
 */
public record SaleItemResponse(
        UUID productId,
        String productSku,
        String productName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {

    /**
     * Maps a sale item entity into its API response form.
     *
     * @param item source sale item entity
     * @return sale item response
     */
    public static SaleItemResponse from(SaleItem item) {
        BigDecimal unitPrice = item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice();
        return new SaleItemResponse(
                item.getProduct().getId(),
                item.getProduct().getSku(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                unitPrice.multiply(item.getQuantity())
        );
    }
}
