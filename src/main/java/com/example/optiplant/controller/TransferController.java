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

/**
 * REST controller for inter-branch transfer workflows.
 */
@RestController
@RequestMapping("/api/transfers")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    /**
     * Lists transfers.
     *
     * @return transfer summaries
     */
    @GetMapping
    public List<TransferResponse> findAll() {
        return transferService.findAll();
    }

    /**
     * Lists transfers sent from a branch.
     *
     * @param branchId source branch identifier
     * @return matching transfers
     */
    @GetMapping("/from/{branchId}")
    public List<TransferResponse> findByFromBranch(@PathVariable UUID branchId) {
        return transferService.findByFromBranchId(branchId);
    }

    /**
     * Lists transfers sent to a branch.
     *
     * @param branchId destination branch identifier
     * @return matching transfers
     */
    @GetMapping("/to/{branchId}")
    public List<TransferResponse> findByToBranch(@PathVariable UUID branchId) {
        return transferService.findByToBranchId(branchId);
    }

    /**
     * Finds a transfer by identifier.
     *
     * @param id transfer identifier
     * @return matching transfer
     */
    @GetMapping("/{id}")
    public TransferResponse findById(@PathVariable UUID id) {
        return transferService.findById(id);
    }

    /**
     * Creates a pending transfer.
     *
     * @param request validated transfer payload
     * @return created transfer
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse create(@Valid @RequestBody TransferRequest request) {
        return transferService.create(request);
    }

    /**
     * Creates and completes a transfer in a single request.
     *
     * @param request validated transfer payload
     * @return completed transfer
     */
    @PostMapping("/complete")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse createCompleted(@Valid @RequestBody TransferRequest request) {
        return transferService.createCompleted(request);
    }

    /**
     * Approves a pending transfer and registers stock leaving the origin branch.
     *
     * @param id transfer identifier
     * @return approved transfer
     */
    @PostMapping("/{id}/approve")
    public TransferResponse approve(@PathVariable UUID id) {
        return transferService.approve(id);
    }

    /**
     * Completes an in-transit transfer and registers stock entering the destination branch.
     *
     * @param id transfer identifier
     * @return completed transfer
     */
    @PostMapping("/{id}/complete")
    public TransferResponse complete(@PathVariable UUID id) {
        return transferService.complete(id);
    }

    /**
     * Cancels a transfer and restores stock when needed.
     *
     * @param id transfer identifier
     * @return cancelled transfer
     */
    @PostMapping("/{id}/cancel")
    public TransferResponse cancel(@PathVariable UUID id) {
        return transferService.cancel(id);
    }
}
