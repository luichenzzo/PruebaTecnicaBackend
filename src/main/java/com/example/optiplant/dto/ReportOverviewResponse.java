package com.example.optiplant.dto;

public record ReportOverviewResponse(
        long totalProducts,
        long totalBranches,
        long totalSales,
        long totalPurchaseOrders,
        long totalTransfers,
        long totalInventoryMovements
) {
}
