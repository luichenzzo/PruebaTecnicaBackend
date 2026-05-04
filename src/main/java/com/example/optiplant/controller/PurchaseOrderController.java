package com.example.optiplant.controller;

import com.example.optiplant.dto.PurchaseOrderRequest;
import com.example.optiplant.dto.PurchaseOrderResponse;
import com.example.optiplant.services.PurchaseOrderService;
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

@RestController
@RequestMapping("/api/purchase-orders")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @GetMapping
    public List<PurchaseOrderResponse> findAll() {
        return purchaseOrderService.findAll();
    }

    @GetMapping("/{id}")
    public PurchaseOrderResponse findById(@PathVariable UUID id) {
        return purchaseOrderService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderResponse create(@Valid @RequestBody PurchaseOrderRequest request) {
        return purchaseOrderService.create(request);
    }

    @PostMapping("/{id}/receive")
    public PurchaseOrderResponse receive(@PathVariable UUID id) {
        return purchaseOrderService.receive(id);
    }

    @PostMapping("/{id}/cancel")
    public PurchaseOrderResponse cancel(@PathVariable UUID id) {
        return purchaseOrderService.cancel(id);
    }


}
