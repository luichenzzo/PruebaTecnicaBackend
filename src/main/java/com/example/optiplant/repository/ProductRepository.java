package com.example.optiplant.repository;

import com.example.optiplant.model.Product;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for product catalog persistence operations.
 */
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Checks whether a SKU is already assigned.
     *
     * @param sku product SKU
     * @return {@code true} when the SKU exists
     */
    boolean existsBySku(String sku);

    /**
     * Finds a product by SKU.
     *
     * @param sku product SKU
     * @return optional matching product
     */
    Optional<Product> findBySku(String sku);
}
