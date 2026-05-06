package com.example.optiplant.repository;

import com.example.optiplant.model.Transfer;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for transfer persistence operations.
 */
public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    /**
     * Checks whether a transfer number is already assigned.
     *
     * @param transferNumber transfer number
     * @return {@code true} when the transfer number exists
     */
    boolean existsByTransferNumber(String transferNumber);

    /**
     * Finds transfers by origin branch.
     *
     * @param branchId origin branch identifier
     * @return transfers sent from the branch
     */
    java.util.List<Transfer> findByFromBranchId(UUID branchId);

    /**
     * Finds transfers by destination branch.
     *
     * @param branchId destination branch identifier
     * @return transfers sent to the branch
     */
    java.util.List<Transfer> findByToBranchId(UUID branchId);
}
