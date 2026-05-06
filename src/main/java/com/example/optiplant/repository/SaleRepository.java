package com.example.optiplant.repository;

import com.example.optiplant.model.Sale;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for sale persistence operations.
 */
public interface SaleRepository extends JpaRepository<Sale, UUID> {

    /**
     * Checks whether a sale number is already assigned.
     *
     * @param saleNumber sale number
     * @return {@code true} when the sale number exists
     */
    boolean existsBySaleNumber(String saleNumber);

    /**
     * Finds sales for a branch.
     *
     * @param branchId branch identifier
     * @return sales registered at the branch
     */
    java.util.List<Sale> findByBranchId(UUID branchId);
}
