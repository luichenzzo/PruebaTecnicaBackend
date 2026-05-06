package com.example.optiplant.dto;

import com.example.optiplant.model.Inventory;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response payload for current product inventory at a branch.
 */
public record InventoryResponse(
        UUID id,
        UUID productId,
        String productSku,
        String productName,
        UUID branchId,
        BigDecimal quantity,
        BigDecimal reserved,
        UUID createdById,
        UUID updatedById
) {

    /**
     * Maps an inventory entity into its API response form.
     *
     * @param inventory source inventory entity
     * @return inventory response
     */
    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getProduct().getId(),
                inventory.getProduct().getSku(),
                inventory.getProduct().getName(),
                inventory.getBranch().getId(),
                inventory.getQuantity(),
                inventory.getReserved(),
                inventory.getCreatedBy() == null ? null : inventory.getCreatedBy().getId(),
                inventory.getUpdatedBy() == null ? null : inventory.getUpdatedBy().getId()
        );
    }
}
