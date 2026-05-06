package com.example.optiplant.repository;

import com.example.optiplant.model.Supplier;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for supplier persistence operations.
 */
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
}
