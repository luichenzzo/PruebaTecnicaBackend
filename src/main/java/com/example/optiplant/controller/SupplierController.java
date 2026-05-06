package com.example.optiplant.controller;

import com.example.optiplant.dto.SupplierResponse;
import com.example.optiplant.services.SupplierService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for supplier lookup endpoints.
 */
@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /**
     * Lists suppliers.
     *
     * @return supplier summaries
     */
    @GetMapping
    public List<SupplierResponse> findAll() {
        return supplierService.findAll();
    }

    /**
     * Finds a supplier by identifier.
     *
     * @param id supplier identifier
     * @return matching supplier
     */
    @GetMapping("/{id}")
    public SupplierResponse findById(@PathVariable UUID id) {
        return supplierService.findById(id);
    }
}

