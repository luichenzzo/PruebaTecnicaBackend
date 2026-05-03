package com.example.optiplant.repository;

import com.example.optiplant.model.Branch;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, UUID> {
}
