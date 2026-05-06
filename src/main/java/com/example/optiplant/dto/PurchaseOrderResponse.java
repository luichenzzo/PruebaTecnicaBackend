package com.example.optiplant.dto;

import com.example.optiplant.model.PurchaseOrder;
import com.example.optiplant.model.enums.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response payload for purchase order workflow state and line items.
 */
public record PurchaseOrderResponse(
        UUID id,
        String orderNumber,
        UUID supplierId,
        UUID branchId,
        OrderStatus status,
        BigDecimal total,
        List<PurchaseOrderItemResponse> items,
        UUID createdById,
        UUID updatedById
) {

    /**
     * Maps a purchase order entity into its API response form.
     *
     * @param order source purchase order entity
     * @return purchase order response
     */
    public static PurchaseOrderResponse from(PurchaseOrder order) {
        return new PurchaseOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getSupplier().getId(),
                order.getBranch().getId(),
                order.getStatus(),
                order.getTotal(),
                order.getItems().stream().map(PurchaseOrderItemResponse::from).toList(),
                order.getCreatedBy() == null ? null : order.getCreatedBy().getId(),
                order.getUpdatedBy() == null ? null : order.getUpdatedBy().getId()
        );
    }
}
