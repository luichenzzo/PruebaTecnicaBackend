package com.example.optiplant.controller;

import com.example.optiplant.dto.InventorySummaryResponse;
import com.example.optiplant.dto.ReportOverviewResponse;
import com.example.optiplant.services.ReportService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/overview")
    public ReportOverviewResponse overview() {
        return reportService.overview();
    }

    @GetMapping("/inventory-summary")
    public List<InventorySummaryResponse> consolidatedInventory() {
        return reportService.consolidatedInventory();
    }
}
