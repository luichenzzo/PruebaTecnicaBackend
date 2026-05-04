package com.example.optiplant.repository;

import com.example.optiplant.model.InventoryMovement;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID> {

    List<InventoryMovement> findByInventoryId(UUID inventoryId);

    @Query("""
            select m from InventoryMovement m
            join fetch m.inventory i
            join fetch i.product p
            join fetch i.branch b
            left join fetch m.createdBy cb
            order by m.createdAt desc
            """)
    List<InventoryMovement> findAllDetailed();

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
