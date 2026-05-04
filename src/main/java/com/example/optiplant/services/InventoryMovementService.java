package com.example.optiplant.services;

import com.example.optiplant.dto.InventoryMovementResponse;
import com.example.optiplant.repository.InventoryMovementRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class InventoryMovementService {

    private final InventoryMovementRepository movementRepository;

    public InventoryMovementService(InventoryMovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    public List<InventoryMovementResponse> findAll() {
        return movementRepository.findAllDetailed().stream().map(InventoryMovementResponse::from).toList();
    }

    public List<InventoryMovementResponse> findByInventory(UUID inventoryId) {
        return movementRepository.findDetailedByInventoryId(inventoryId).stream()
                .map(InventoryMovementResponse::from)
                .toList();
    }
}
