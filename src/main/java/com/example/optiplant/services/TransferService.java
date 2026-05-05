package com.example.optiplant.services;

import com.example.optiplant.dto.OrderItemRequest;
import com.example.optiplant.dto.TransferRequest;
import com.example.optiplant.dto.TransferResponse;
import com.example.optiplant.exceptions.BadRequestException;
import com.example.optiplant.exceptions.NotFoundException;
import com.example.optiplant.model.Branch;
import com.example.optiplant.model.Product;
import com.example.optiplant.model.Transfer;
import com.example.optiplant.model.TransferItem;
import com.example.optiplant.model.enums.MovementType;
import com.example.optiplant.model.enums.TransferStatus;
import com.example.optiplant.repository.BranchRepository;
import com.example.optiplant.repository.ProductRepository;
import com.example.optiplant.repository.TransferRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TransferService {

    private static final DateTimeFormatter NUMBER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final TransferRepository transferRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;
    private final InventoryService inventoryService;
    private final RealtimeNotificationService realtimeNotificationService;

    public TransferService(
            TransferRepository transferRepository,
            BranchRepository branchRepository,
            ProductRepository productRepository,
            CurrentUserService currentUserService,
            InventoryService inventoryService,
            RealtimeNotificationService realtimeNotificationService
    ) {
        this.transferRepository = transferRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.currentUserService = currentUserService;
        this.inventoryService = inventoryService;
        this.realtimeNotificationService = realtimeNotificationService;
    }

    public List<TransferResponse> findAll() {
        return transferRepository.findAll().stream().map(TransferResponse::from).toList();
    }

    public List<TransferResponse> findByFromBranchId(UUID branchId) {
        return transferRepository.findByFromBranchId(branchId).stream().map(TransferResponse::from).toList();
    }

    public List<TransferResponse> findByToBranchId(UUID branchId) {
        return transferRepository.findByToBranchId(branchId).stream().map(TransferResponse::from).toList();
    }

    public TransferResponse findById(UUID id) {
        return TransferResponse.from(getTransfer(id));
    }

    @Transactional
    public TransferResponse create(TransferRequest request) {
        currentUserService.getAuthenticatedUser();
        Transfer saved = transferRepository.save(buildTransfer(request));
        publishTransfer(saved);
        return TransferResponse.from(saved);
    }

    @Transactional
    public TransferResponse createCompleted(TransferRequest request) {
        currentUserService.getAuthenticatedUser();
        Transfer transfer = transferRepository.save(buildTransfer(request));

        registerTransferOut(transfer, "Transfer completed");
        registerTransferIn(transfer, "Transfer completed");

        transfer.setStatus(TransferStatus.COMPLETED);
        Transfer saved = transferRepository.save(transfer);
        publishTransfer(saved);
        return TransferResponse.from(saved);
    }

    @Transactional
    public TransferResponse approve(UUID id) {
        currentUserService.getAuthenticatedUser();
        Transfer transfer = getTransfer(id);

        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new BadRequestException("Only pending transfers can be approved");
        }

        registerTransferOut(transfer, "Transfer approved");

        transfer.setStatus(TransferStatus.IN_TRANSIT);
        Transfer saved = transferRepository.save(transfer);
        publishTransfer(saved);
        return TransferResponse.from(saved);
    }

    @Transactional
    public TransferResponse complete(UUID id) {
        currentUserService.getAuthenticatedUser();
        Transfer transfer = getTransfer(id);

        if (transfer.getStatus() != TransferStatus.IN_TRANSIT) {
            throw new BadRequestException("Only in-transit transfers can be completed");
        }

        registerTransferIn(transfer, "Transfer completed");

        transfer.setStatus(TransferStatus.COMPLETED);
        Transfer saved = transferRepository.save(transfer);
        publishTransfer(saved);
        return TransferResponse.from(saved);
    }

    @Transactional
    public TransferResponse cancel(UUID id) {
        currentUserService.getAuthenticatedUser();
        Transfer transfer = getTransfer(id);

        if (transfer.getStatus() == TransferStatus.CANCELLED) {
            throw new BadRequestException("Transfer is already cancelled");
        }

        if (transfer.getStatus() == TransferStatus.COMPLETED) {
            throw new BadRequestException("Completed transfers cannot be cancelled");
        }

        if (transfer.getStatus() == TransferStatus.IN_TRANSIT) {
            restoreTransferOut(transfer);
        }

        transfer.setStatus(TransferStatus.CANCELLED);
        Transfer saved = transferRepository.save(transfer);
        publishTransfer(saved);
        return TransferResponse.from(saved);
    }

    private Transfer buildTransfer(TransferRequest request) {
        if (request.fromBranchId().equals(request.toBranchId())) {
            throw new BadRequestException("Origin and destination branches must be different");
        }

        Branch fromBranch = branchRepository.findById(request.fromBranchId())
                .orElseThrow(() -> new NotFoundException("Origin branch not found"));
        Branch toBranch = branchRepository.findById(request.toBranchId())
                .orElseThrow(() -> new NotFoundException("Destination branch not found"));
        String transferNumber = request.transferNumber() == null || request.transferNumber().isBlank()
                ? "TRF-" + LocalDateTime.now().format(NUMBER_FORMAT)
                : request.transferNumber().trim();

        if (transferRepository.existsByTransferNumber(transferNumber)) {
            throw new BadRequestException("Transfer number is already registered");
        }

        Transfer transfer = new Transfer();
        transfer.setTransferNumber(transferNumber);
        transfer.setFromBranch(fromBranch);
        transfer.setToBranch(toBranch);
        transfer.setStatus(TransferStatus.PENDING);

        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            TransferItem transferItem = new TransferItem();
            transferItem.setTransfer(transfer);
            transferItem.setProduct(product);
            transferItem.setQuantity(itemRequest.quantity());
            transfer.getItems().add(transferItem);
        }

        return transfer;
    }

    private void registerTransferOut(Transfer transfer, String notes) {
        for (TransferItem item : transfer.getItems()) {
            inventoryService.decreaseStock(
                    item.getProduct().getId(),
                    transfer.getFromBranch().getId(),
                    item.getQuantity(),
                    MovementType.TRANSFER_OUT,
                    transfer.getTransferNumber(),
                    notes,
                    "TRANSFER",
                    transfer.getId().toString()
            );
        }
    }

    private void registerTransferIn(Transfer transfer, String notes) {
        for (TransferItem item : transfer.getItems()) {
            inventoryService.increaseStock(
                    item.getProduct().getId(),
                    transfer.getToBranch().getId(),
                    item.getQuantity(),
                    MovementType.TRANSFER_IN,
                    transfer.getTransferNumber(),
                    notes,
                    "TRANSFER",
                    transfer.getId().toString()
            );
        }
    }

    private void restoreTransferOut(Transfer transfer) {
        for (TransferItem item : transfer.getItems()) {
            inventoryService.increaseStock(
                    item.getProduct().getId(),
                    transfer.getFromBranch().getId(),
                    item.getQuantity(),
                    MovementType.ADJUSTMENT,
                    transfer.getTransferNumber(),
                    "Transfer cancelled",
                    "TRANSFER",
                    transfer.getId().toString()
            );
        }
    }

    private void publishTransfer(Transfer transfer) {
        realtimeNotificationService.transferUpdated(TransferResponse.from(transfer));
    }

    private Transfer getTransfer(UUID id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transfer not found"));
    }
}
