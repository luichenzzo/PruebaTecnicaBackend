package com.example.optiplant.services;

import com.example.optiplant.dto.InventoryMovementResponse;
import com.example.optiplant.repository.InventoryMovementRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Provides read access to detailed inventory movement history.
 */
@Service
public class InventoryMovementService {

    private final InventoryMovementRepository movementRepository;

    public InventoryMovementService(InventoryMovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    /**
     * Lists all inventory movements with their related product and branch data.
     *
     * @return movement responses
     */
    public List<InventoryMovementResponse> findAll() {
        return movementRepository.findAllDetailed().stream().map(InventoryMovementResponse::from).toList();
    }

    /**
     * Lists movement history for a specific inventory row.
     *
     * @param inventoryId inventory identifier
     * @return movement responses
     */
    public List<InventoryMovementResponse> findByInventory(UUID inventoryId) {
        return movementRepository.findDetailedByInventoryId(inventoryId).stream()
                .map(InventoryMovementResponse::from)
                .toList();
    }
}
