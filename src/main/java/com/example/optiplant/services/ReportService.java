package com.example.optiplant.services;

import com.example.optiplant.dto.InventorySummaryResponse;
import com.example.optiplant.dto.ReportOverviewResponse;
import com.example.optiplant.repository.BranchRepository;
import com.example.optiplant.repository.InventoryMovementRepository;
import com.example.optiplant.repository.InventoryRepository;
import com.example.optiplant.repository.ProductRepository;
import com.example.optiplant.repository.PurchaseOrderRepository;
import com.example.optiplant.repository.SaleRepository;
import com.example.optiplant.repository.TransferRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Builds aggregate metrics and inventory summaries for reporting endpoints.
 */
@Service
public class ReportService {

    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;
    private final SaleRepository saleRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final TransferRepository transferRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryRepository inventoryRepository;

    public ReportService(
            ProductRepository productRepository,
            BranchRepository branchRepository,
            SaleRepository saleRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            TransferRepository transferRepository,
            InventoryMovementRepository inventoryMovementRepository,
            InventoryRepository inventoryRepository
    ) {
        this.productRepository = productRepository;
        this.branchRepository = branchRepository;
        this.saleRepository = saleRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.transferRepository = transferRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Calculates high-level entity counts for the dashboard overview.
     *
     * @return overview metrics
     */
    public ReportOverviewResponse overview() {
        return new ReportOverviewResponse(
                productRepository.count(),
                branchRepository.count(),
                saleRepository.count(),
                purchaseOrderRepository.count(),
                transferRepository.count(),
                inventoryMovementRepository.count()
        );
    }

    /**
     * Groups inventory by product and sums quantity and reserved values.
     *
     * @return consolidated inventory rows
     */
    public List<InventorySummaryResponse> consolidatedInventory() {
        Map<UUID, List<com.example.optiplant.model.Inventory>> byProduct = inventoryRepository.findAllWithProductAndBranch()
                .stream()
                .collect(Collectors.groupingBy(inventory -> inventory.getProduct().getId()));

        return byProduct.values().stream()
                .map(inventories -> {
                    com.example.optiplant.model.Inventory first = inventories.getFirst();
                    BigDecimal quantity = inventories.stream()
                            .map(com.example.optiplant.model.Inventory::getQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal reserved = inventories.stream()
                            .map(com.example.optiplant.model.Inventory::getReserved)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new InventorySummaryResponse(
                            first.getProduct().getId(),
                            first.getProduct().getSku(),
                            first.getProduct().getName(),
                            quantity,
                            reserved
                    );
                })
                .toList();
    }
}
