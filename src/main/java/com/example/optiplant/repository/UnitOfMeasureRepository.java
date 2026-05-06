package com.example.optiplant.repository;

import com.example.optiplant.model.UnitOfMeasure;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for unit-of-measure persistence operations.
 */
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, UUID> {
}
