package com.example.optiplant.dto;

/**
 * High-level counts used by the reporting overview endpoint.
 */
public record ReportOverviewResponse(
        long totalProducts,
        long totalBranches,
        long totalSales,
        long totalPurchaseOrders,
        long totalTransfers,
        long totalInventoryMovements
) {
}
