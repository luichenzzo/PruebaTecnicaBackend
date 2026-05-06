package com.example.optiplant.dto;

import com.example.optiplant.model.Transfer;
import com.example.optiplant.model.enums.TransferStatus;
import java.util.List;
import java.util.UUID;

/**
 * Response payload for transfer workflow state and line items.
 */
public record TransferResponse(
        UUID id,
        String transferNumber,
        UUID fromBranchId,
        UUID toBranchId,
        TransferStatus status,
        List<TransferItemResponse> items,
        UUID createdById,
        UUID updatedById
) {

    /**
     * Maps a transfer entity into its API response form.
     *
     * @param transfer source transfer entity
     * @return transfer response
     */
    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getTransferNumber(),
                transfer.getFromBranch().getId(),
                transfer.getToBranch().getId(),
                transfer.getStatus(),
                transfer.getItems().stream().map(TransferItemResponse::from).toList(),
                transfer.getCreatedBy() == null ? null : transfer.getCreatedBy().getId(),
                transfer.getUpdatedBy() == null ? null : transfer.getUpdatedBy().getId()
        );
    }
}
