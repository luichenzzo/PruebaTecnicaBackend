package com.example.optiplant.services;

import com.example.optiplant.dto.InventoryAdjustmentRequest;
import com.example.optiplant.dto.InventoryResponse;
import com.example.optiplant.exceptions.BadRequestException;
import com.example.optiplant.exceptions.NotFoundException;
import com.example.optiplant.model.Branch;
import com.example.optiplant.model.Inventory;
import com.example.optiplant.model.InventoryMovement;
import com.example.optiplant.model.Product;
import com.example.optiplant.model.enums.MovementType;
import com.example.optiplant.repository.BranchRepository;
import com.example.optiplant.repository.InventoryMovementRepository;
import com.example.optiplant.repository.InventoryRepository;
import com.example.optiplant.repository.ProductRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;
    private final CurrentUserService currentUserService;

    public InventoryService(
            InventoryRepository inventoryRepository,
            InventoryMovementRepository movementRepository,
            ProductRepository productRepository,
            BranchRepository branchRepository,
            CurrentUserService currentUserService
    ) {
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
        this.productRepository = productRepository;
        this.branchRepository = branchRepository;
        this.currentUserService = currentUserService;
    }

    public List<InventoryResponse> findAll(UUID branchId) {
        List<Inventory> inventories = branchId == null
                ? inventoryRepository.findAll()
                : inventoryRepository.findByBranchId(branchId);
        return inventories.stream().map(InventoryResponse::from).toList();
    }

    public InventoryResponse findById(UUID id) {
        return InventoryResponse.from(getInventory(id));
    }

    public InventoryResponse findByProductAndBranch(UUID productId, UUID branchId) {
        return InventoryResponse.from(inventoryRepository.findByProductIdAndBranchId(productId, branchId)
                .orElseThrow(() -> new NotFoundException("Inventory not found for product and branch")));
    }

    @Transactional
    public InventoryResponse adjustQuantity(InventoryAdjustmentRequest request) {
        currentUserService.getAuthenticatedUser();

        Inventory inventory = getOrCreateInventory(request.productId(), request.branchId());
        BigDecimal previousQuantity = inventory.getQuantity();
        inventory.setQuantity(request.quantity());
        Inventory savedInventory = inventoryRepository.save(inventory);

        registerMovement(
                savedInventory,
                MovementType.ADJUSTMENT,
                request.quantity().subtract(previousQuantity).abs(),
                "INVENTORY_ADJUSTMENT",
                request.notes(),
                "ADJUSTMENT",
                savedInventory.getId().toString()
        );

        return InventoryResponse.from(savedInventory);
    }

    Inventory increaseStock(
            UUID productId,
            UUID branchId,
            BigDecimal quantity,
            MovementType movementType,
            String reference,
            String notes,
            String sourceType,
            String sourceId
    ) {
        validateQuantity(quantity);
        Inventory inventory = getOrCreateInventory(productId, branchId);
        inventory.setQuantity(inventory.getQuantity().add(quantity));
        Inventory savedInventory = inventoryRepository.save(inventory);
        registerMovement(savedInventory, movementType, quantity, reference, notes, sourceType, sourceId);
        return savedInventory;
    }

    Inventory decreaseStock(
            UUID productId,
            UUID branchId,
            BigDecimal quantity,
            MovementType movementType,
            String reference,
            String notes,
            String sourceType,
            String sourceId
    ) {
        validateQuantity(quantity);
        Inventory inventory = inventoryRepository.findByProductIdAndBranchId(productId, branchId)
                .orElseThrow(() -> new BadRequestException("Inventory does not exist for product and branch"));

        if (inventory.getQuantity().compareTo(quantity) < 0) {
            throw new BadRequestException("Insufficient stock for product " + inventory.getProduct().getSku());
        }

        inventory.setQuantity(inventory.getQuantity().subtract(quantity));
        Inventory savedInventory = inventoryRepository.save(inventory);
        registerMovement(savedInventory, movementType, quantity, reference, notes, sourceType, sourceId);
        return savedInventory;
    }

    void registerMovement(
            Inventory inventory,
            MovementType type,
            BigDecimal quantity,
            String reference,
            String notes,
            String sourceType,
            String sourceId
    ) {
        InventoryMovement movement = new InventoryMovement();
        movement.setInventory(inventory);
        movement.setMovementType(type);
        movement.setQuantity(quantity);
        movement.setReference(reference);
        movement.setNotes(notes);
        movement.setSourceType(sourceType);
        movement.setSourceId(sourceId);
        movementRepository.save(movement);
    }

    Inventory getInventory(UUID id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inventory not found"));
    }

    Inventory getOrCreateInventory(UUID productId, UUID branchId) {
        return inventoryRepository.findByProductIdAndBranchId(productId, branchId)
                .orElseGet(() -> {
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new NotFoundException("Product not found"));
                    Branch branch = branchRepository.findById(branchId)
                            .orElseThrow(() -> new NotFoundException("Branch not found"));

                    Inventory inventory = new Inventory();
                    inventory.setProduct(product);
                    inventory.setBranch(branch);
                    inventory.setQuantity(BigDecimal.ZERO);
                    inventory.setReserved(BigDecimal.ZERO);
                    return inventoryRepository.save(inventory);
                });
    }

    private void validateQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }
    }
}
