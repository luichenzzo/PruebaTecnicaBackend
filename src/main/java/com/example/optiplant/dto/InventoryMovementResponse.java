package com.example.optiplant.dto;

import com.example.optiplant.model.InventoryMovement;
import com.example.optiplant.model.enums.MovementType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response payload for a single auditable inventory movement.
 */
public record InventoryMovementResponse(
        UUID id,
        UUID inventoryId,
        UUID productId,
        String productSku,
        UUID branchId,
        String movementCategory,
        MovementType movementType,
        BigDecimal quantity,
        String reference,
        String notes,
        String sourceType,
        String sourceId,
        LocalDateTime createdAt,
        UUID createdById
) {

    /**
     * Maps an inventory movement entity into its API response form.
     *
     * @param movement source inventory movement entity
     * @return inventory movement response
     */
    public static InventoryMovementResponse from(InventoryMovement movement) {
        return new InventoryMovementResponse(
                movement.getId(),
                movement.getInventory().getId(),
                movement.getInventory().getProduct().getId(),
                movement.getInventory().getProduct().getSku(),
                movement.getInventory().getBranch().getId(),
                toCategory(movement.getMovementType()),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getReference(),
                movement.getNotes(),
                movement.getSourceType(),
                movement.getSourceId(),
                movement.getCreatedAt(),
                movement.getCreatedBy() == null ? null : movement.getCreatedBy().getId()
        );
    }

    private static String toCategory(MovementType movementType) {
        return switch (movementType) {
            case PURCHASE_IN -> "IN";
            case SALE_OUT -> "OUT";
            case ADJUSTMENT -> "ADJUSTMENT";
            case TRANSFER_IN, TRANSFER_OUT -> "TRANSFER";
        };
    }
}
