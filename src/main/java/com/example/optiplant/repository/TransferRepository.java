package com.example.optiplant.repository;

import com.example.optiplant.model.Transfer;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    boolean existsByTransferNumber(String transferNumber);

    java.util.List<Transfer> findByFromBranchId(UUID branchId);

    java.util.List<Transfer> findByToBranchId(UUID branchId);
}
