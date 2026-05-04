package com.example.optiplant.repository;

import com.example.optiplant.model.PurchaseOrder;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {

    boolean existsByOrderNumber(String orderNumber);
}
