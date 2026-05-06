package com.example.optiplant.controller;

import com.example.optiplant.dto.SaleRequest;
import com.example.optiplant.dto.SaleResponse;
import com.example.optiplant.services.SaleService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for sales and sale cancellation workflows.
 */
@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }


    /**
     * Lists sales, optionally filtered by branch.
     *
     * @param branchId optional branch identifier
     * @return sale summaries
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    public List<SaleResponse> findAll(@org.springframework.web.bind.annotation.RequestParam(required = false) UUID branchId) {
        return saleService.findAll(branchId);
    }

    /**
     * Finds a sale by identifier.
     *
     * @param id sale identifier
     * @return matching sale
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    public SaleResponse findById(@PathVariable UUID id) {
        return saleService.findById(id);
    }

    /**
     * Creates a completed sale and decreases inventory.
     *
     * @param request validated sale payload
     * @return created sale
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    public SaleResponse create(@Valid @RequestBody SaleRequest request) {
        return saleService.create(request);
    }

    /**
     * Cancels a completed sale and restores stock.
     *
     * @param id sale identifier
     * @return cancelled sale
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public SaleResponse cancel(@PathVariable UUID id) {
        return saleService.cancel(id);
    }
}
