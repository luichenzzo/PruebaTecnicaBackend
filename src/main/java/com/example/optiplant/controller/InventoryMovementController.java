package com.example.optiplant.controller;

import com.example.optiplant.dto.InventoryMovementResponse;
import com.example.optiplant.services.InventoryMovementService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for auditable inventory movement history.
 */
@RestController
@RequestMapping("/api/inventory-movements")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class InventoryMovementController {

    private final InventoryMovementService movementService;

    public InventoryMovementController(InventoryMovementService movementService) {
        this.movementService = movementService;
    }

    /**
     * Lists inventory movements, optionally filtered by inventory row.
     *
     * @param inventoryId optional inventory identifier
     * @return movement history
     */
    @GetMapping
    public List<InventoryMovementResponse> findAll(@RequestParam(required = false) UUID inventoryId) {
        if (inventoryId != null) {
            return movementService.findByInventory(inventoryId);
        }
        return movementService.findAll();
    }
}
