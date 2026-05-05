package com.example.optiplant.services;

import com.example.optiplant.dto.BranchRequest;
import com.example.optiplant.dto.BranchResponse;
import com.example.optiplant.exceptions.ForbiddenException;
import com.example.optiplant.exceptions.NotFoundException;
import com.example.optiplant.model.Branch;
import com.example.optiplant.model.User;
import com.example.optiplant.model.enums.Role;
import com.example.optiplant.repository.BranchRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BranchService {

    private final BranchRepository branchRepository;
    private final CurrentUserService currentUserService;

    public BranchService(BranchRepository branchRepository, CurrentUserService currentUserService) {
        this.branchRepository = branchRepository;
        this.currentUserService = currentUserService;
    }

    public List<BranchResponse> findAll() {
        return branchRepository.findAll().stream().map(BranchResponse::from).toList();
    }

    public BranchResponse findById(UUID id) {
        return BranchResponse.from(getBranch(id));
    }

    @Transactional
    public BranchResponse create(BranchRequest request) {
        User user = currentUserService.getAuthenticatedUser();
        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can create branches");
        }
        Branch branch = new Branch();
        branch.setCode(request.code().trim());
        branch.setName(request.name().trim());
        branch.setAddress(request.address() == null ? null : request.address().trim());

        return BranchResponse.from(branchRepository.save(branch));
    }

    @Transactional
    public BranchResponse update(UUID id, BranchRequest request) {
        User user = currentUserService.getAuthenticatedUser();
        if (user.getRole() != Role.MANAGER) {
            throw new ForbiddenException("Only managers can update branches");
        }

        Branch branch = getBranch(id);
        branch.setCode(request.code().trim());
        branch.setName(request.name().trim());
        branch.setAddress(request.address() == null ? null : request.address().trim());

        return BranchResponse.from(branchRepository.save(branch));
    }

    @Transactional
    public void delete(UUID id) {
        User user = currentUserService.getAuthenticatedUser();
        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only managers can delete branches");
        }

        Branch branch = getBranch(id);
        branchRepository.delete(branch);
    }

    private Branch getBranch(UUID id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch not found"));
    }
}

