package com.example.optiplant.controller;

import com.example.optiplant.dto.BranchRequest;
import com.example.optiplant.dto.BranchResponse;
import com.example.optiplant.services.BranchService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for branch catalog operations.
 */
@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    /**
     * Lists all branches visible to authenticated users.
     *
     * @return branch summaries
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    public List<BranchResponse> findAll() {
        return branchService.findAll();
    }

    /**
     * Finds a branch by identifier.
     *
     * @param id branch identifier
     * @return matching branch
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    public BranchResponse findById(@PathVariable UUID id) {
        return branchService.findById(id);
    }

    /**
     * Creates a new branch.
     *
     * @param request validated branch payload
     * @return created branch
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public BranchResponse create(@Valid @RequestBody BranchRequest request) {
        return branchService.create(request);
    }

    /**
     * Updates an existing branch.
     *
     * @param id branch identifier
     * @param request validated branch payload
     * @return updated branch
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BranchResponse update(@PathVariable UUID id, @Valid @RequestBody BranchRequest request) {
        return branchService.update(id, request);
    }

    /**
     * Deletes a branch.
     *
     * @param id branch identifier
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id) {
        branchService.delete(id);
    }
}

