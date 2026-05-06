package com.example.optiplant.dto;

import com.example.optiplant.model.Supplier;
import java.util.UUID;

/**
 * Response payload for supplier details.
 */
public record SupplierResponse(
        UUID id,
        String name,
        String contactName,
        String contactEmail,
        String phone,
        UUID createdById,
        UUID updatedById
) {

    /**
     * Maps a supplier entity into its API response form.
     *
     * @param supplier source supplier entity
     * @return supplier response
     */
    public static SupplierResponse from(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactName(),
                supplier.getContactEmail(),
                supplier.getPhone(),
                supplier.getCreatedBy() == null ? null : supplier.getCreatedBy().getId(),
                supplier.getUpdatedBy() == null ? null : supplier.getUpdatedBy().getId()
        );
    }
}

