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

    public TransferService(
            TransferRepository transferRepository,
            BranchRepository branchRepository,
            ProductRepository productRepository,
            CurrentUserService currentUserService,
            InventoryService inventoryService
    ) {
        this.transferRepository = transferRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.currentUserService = currentUserService;
        this.inventoryService = inventoryService;
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

        return TransferResponse.from(transferRepository.save(transfer));
    }

    @Transactional
    public TransferResponse approve(UUID id) {
        currentUserService.getAuthenticatedUser();
        Transfer transfer = getTransfer(id);

        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new BadRequestException("Only pending transfers can be approved");
        }

        for (TransferItem item : transfer.getItems()) {
            inventoryService.decreaseStock(
                    item.getProduct().getId(),
                    transfer.getFromBranch().getId(),
                    item.getQuantity(),
                    MovementType.TRANSFER_OUT,
                    transfer.getTransferNumber(),
                    "Transfer approved",
                    "TRANSFER",
                    transfer.getId().toString()
            );
        }

        transfer.setStatus(TransferStatus.IN_TRANSIT);
        return TransferResponse.from(transferRepository.save(transfer));
    }

    @Transactional
    public TransferResponse complete(UUID id) {
        currentUserService.getAuthenticatedUser();
        Transfer transfer = getTransfer(id);

        if (transfer.getStatus() != TransferStatus.IN_TRANSIT) {
            throw new BadRequestException("Only in-transit transfers can be completed");
        }

        for (TransferItem item : transfer.getItems()) {
            inventoryService.increaseStock(
                    item.getProduct().getId(),
                    transfer.getToBranch().getId(),
                    item.getQuantity(),
                    MovementType.TRANSFER_IN,
                    transfer.getTransferNumber(),
                    "Transfer completed",
                    "TRANSFER",
                    transfer.getId().toString()
            );
        }

        transfer.setStatus(TransferStatus.COMPLETED);
        return TransferResponse.from(transferRepository.save(transfer));
    }

    private Transfer getTransfer(UUID id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transfer not found"));
    }
}
