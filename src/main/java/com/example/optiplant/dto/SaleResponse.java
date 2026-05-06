package com.example.optiplant.dto;

import com.example.optiplant.model.Sale;
import com.example.optiplant.model.enums.SaleStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response payload for sale workflow state and line items.
 */
public record SaleResponse(
        UUID id,
        String saleNumber,
        UUID branchId,
        SaleStatus status,
        BigDecimal total,
        List<SaleItemResponse> items,
        UUID createdById,
        UUID updatedById
) {

    /**
     * Maps a sale entity into its API response form.
     *
     * @param sale source sale entity
     * @return sale response
     */
    public static SaleResponse from(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getSaleNumber(),
                sale.getBranch().getId(),
                sale.getStatus(),
                sale.getTotal(),
                sale.getItems().stream().map(SaleItemResponse::from).toList(),
                sale.getCreatedBy() == null ? null : sale.getCreatedBy().getId(),
                sale.getUpdatedBy() == null ? null : sale.getUpdatedBy().getId()
        );
    }
}
