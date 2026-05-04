package com.example.optiplant.dto;

import com.example.optiplant.model.Transfer;
import com.example.optiplant.model.enums.TransferStatus;
import java.util.List;
import java.util.UUID;

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
