package com.example.optiplant.repository;

import com.example.optiplant.model.InventoryMovement;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository for inventory movement history queries.
 */
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID> {

    /**
     * Finds movement rows for a specific inventory record.
     *
     * @param inventoryId inventory identifier
     * @return matching movement rows
     */
    List<InventoryMovement> findByInventoryId(UUID inventoryId);

    /**
     * Fetches all movement rows with inventory, product, branch, and creator data
     * eagerly loaded.
     *
     * @return detailed movement rows ordered from newest to oldest
     */
    @Query("""
            select m from InventoryMovement m
            join fetch m.inventory i
            join fetch i.product p
            join fetch i.branch b
            left join fetch m.createdBy cb
            order by m.createdAt desc
            """)
    List<InventoryMovement> findAllDetailed();

    /**
     * Fetches detailed movement rows for a specific inventory record.
     *
     * @param inventoryId inventory identifier
     * @return detailed movement rows ordered from newest to oldest
     */
    @Query("""
            select m from InventoryMovement m
            join fetch m.inventory i
            join fetch i.product p
            join fetch i.branch b
            left join fetch m.createdBy cb
            where i.id = :inventoryId
            order by m.createdAt desc
            """)
    List<InventoryMovement> findDetailedByInventoryId(UUID inventoryId);
}
