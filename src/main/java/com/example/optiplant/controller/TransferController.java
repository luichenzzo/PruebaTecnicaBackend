package com.example.optiplant.controller;

import com.example.optiplant.dto.TransferRequest;
import com.example.optiplant.dto.TransferResponse;
import com.example.optiplant.services.TransferService;
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
@RequestMapping("/api/transfers")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping
    public List<TransferResponse> findAll() {
        return transferService.findAll();
    }

    @GetMapping("/from/{branchId}")
    public List<TransferResponse> findByFromBranch(@PathVariable UUID branchId) {
        return transferService.findByFromBranchId(branchId);
    }

    @GetMapping("/to/{branchId}")
    public List<TransferResponse> findByToBranch(@PathVariable UUID branchId) {
        return transferService.findByToBranchId(branchId);
    }

    @GetMapping("/{id}")
    public TransferResponse findById(@PathVariable UUID id) {
        return transferService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse create(@Valid @RequestBody TransferRequest request) {
        return transferService.create(request);
    }

    @PostMapping("/{id}/approve")
    public TransferResponse approve(@PathVariable UUID id) {
        return transferService.approve(id);
    }

    @PostMapping("/{id}/complete")
    public TransferResponse complete(@PathVariable UUID id) {
        return transferService.complete(id);
    }
}
