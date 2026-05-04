package com.example.optiplant.dto;

import com.example.optiplant.model.TransferItem;
import java.math.BigDecimal;
import java.util.UUID;

public record TransferItemResponse(
        UUID productId,
        String productSku,
        String productName,
        BigDecimal quantity
) {

    public static TransferItemResponse from(TransferItem item) {
        return new TransferItemResponse(
                item.getProduct().getId(),
                item.getProduct().getSku(),
                item.getProduct().getName(),
                item.getQuantity()
        );
    }
}
