package com.example.optiplant.dto;

import com.example.optiplant.model.TransferItem;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response payload for a transfer line item.
 */
public record TransferItemResponse(
        UUID productId,
        String productSku,
        String productName,
        BigDecimal quantity
) {

    /**
     * Maps a transfer item entity into its API response form.
     *
     * @param item source transfer item entity
     * @return transfer item response
     */
    public static TransferItemResponse from(TransferItem item) {
        return new TransferItemResponse(
                item.getProduct().getId(),
                item.getProduct().getSku(),
                item.getProduct().getName(),
                item.getQuantity()
        );
    }
}
