package com.example.optiplant.repository;

import com.example.optiplant.model.Inventory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository for current inventory balances.
 */
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    /**
     * Finds inventory rows for a branch.
     *
     * @param branchId branch identifier
     * @return matching inventory rows
     */
    List<Inventory> findByBranchId(UUID branchId);

    /**
     * Finds the unique inventory row for a product at a branch.
     *
     * @param productId product identifier
     * @param branchId branch identifier
     * @return optional matching inventory row
     */
    Optional<Inventory> findByProductIdAndBranchId(UUID productId, UUID branchId);

    /**
     * Fetches inventory rows with product and branch data eagerly loaded.
     *
     * @return inventory rows with product and branch references
     */
    @Query("""
            select i from Inventory i
            join fetch i.product p
            join fetch i.branch b
            """)
    List<Inventory> findAllWithProductAndBranch();
}
