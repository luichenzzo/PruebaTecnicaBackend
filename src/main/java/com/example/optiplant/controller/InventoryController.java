package com.example.optiplant.controller;

import com.example.optiplant.dto.InventoryAdjustmentRequest;
import com.example.optiplant.dto.InventoryResponse;
import com.example.optiplant.services.InventoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for current inventory queries and manual stock adjustments.
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Lists inventory, optionally filtered by branch.
     *
     * @param branchId optional branch identifier
     * @return inventory rows
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    public List<InventoryResponse> findAll(@RequestParam(required = false) UUID branchId) {
        return inventoryService.findAll(branchId);
    }

    /**
     * Lists inventory for a branch.
     *
     * @param branchId branch identifier
     * @return inventory rows for the branch
     */
    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER')")
    public List<InventoryResponse> findByBranch(@PathVariable UUID branchId) {
        return inventoryService.findAll(branchId);
    }

    /**
     * Finds an inventory row by identifier.
     *
     * @param id inventory identifier
     * @return matching inventory row
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    public InventoryResponse findById(@PathVariable UUID id) {
        return inventoryService.findById(id);
    }

    /**
     * Finds inventory for a product at a branch.
     *
     * @param productId product identifier
     * @param branchId branch identifier
     * @return matching inventory row
     */
    @GetMapping("/lookup")
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    public InventoryResponse findByProductAndBranch(
            @RequestParam UUID productId,
            @RequestParam UUID branchId
    ) {
        return inventoryService.findByProductAndBranch(productId, branchId);
    }

    /**
     * Sets inventory quantity through a manual adjustment.
     *
     * @param request validated adjustment payload
     * @return adjusted inventory row
     */
    @PatchMapping("/adjust")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public InventoryResponse adjustQuantity(@Valid @RequestBody InventoryAdjustmentRequest request) {
        return inventoryService.adjustQuantity(request);
    }
}
