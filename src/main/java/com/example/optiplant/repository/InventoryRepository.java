package com.example.optiplant.repository;

import com.example.optiplant.model.Inventory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    List<Inventory> findByBranchId(UUID branchId);

    Optional<Inventory> findByProductIdAndBranchId(UUID productId, UUID branchId);

    @Query("""
            select i from Inventory i
            join fetch i.product p
            join fetch i.branch b
            """)
    List<Inventory> findAllWithProductAndBranch();
}
