package com.example.optiplant.repository;

import com.example.optiplant.model.PurchaseOrder;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for purchase order persistence operations.
 */
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {

    /**
     * Checks whether a purchase order number is already assigned.
     *
     * @param orderNumber purchase order number
     * @return {@code true} when the order number exists
     */
    boolean existsByOrderNumber(String orderNumber);
}
