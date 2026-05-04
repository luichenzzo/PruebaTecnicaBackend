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

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    public List<InventoryResponse> findAll(@RequestParam(required = false) UUID branchId) {
        return inventoryService.findAll(branchId);
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER')")
    public List<InventoryResponse> findByBranch(@PathVariable UUID branchId) {
        return inventoryService.findAll(branchId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    public InventoryResponse findById(@PathVariable UUID id) {
        return inventoryService.findById(id);
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    public InventoryResponse findByProductAndBranch(
            @RequestParam UUID productId,
            @RequestParam UUID branchId
    ) {
        return inventoryService.findByProductAndBranch(productId, branchId);
    }

    @PatchMapping("/adjust")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public InventoryResponse adjustQuantity(@Valid @RequestBody InventoryAdjustmentRequest request) {
        return inventoryService.adjustQuantity(request);
    }
}
