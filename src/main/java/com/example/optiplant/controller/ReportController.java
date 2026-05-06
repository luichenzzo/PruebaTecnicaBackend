package com.example.optiplant.controller;

import com.example.optiplant.dto.InventorySummaryResponse;
import com.example.optiplant.dto.ReportOverviewResponse;
import com.example.optiplant.services.ReportService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for operational dashboard and reporting endpoints.
 */
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Returns high-level entity counts for reporting dashboards.
     *
     * @return overview metrics
     */
    @GetMapping("/overview")
    public ReportOverviewResponse overview() {
        return reportService.overview();
    }

    /**
     * Returns consolidated inventory quantities grouped by product.
     *
     * @return inventory summary rows
     */
    @GetMapping("/inventory-summary")
    public List<InventorySummaryResponse> consolidatedInventory() {
        return reportService.consolidatedInventory();
    }
}
