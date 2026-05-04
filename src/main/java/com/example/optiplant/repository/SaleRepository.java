package com.example.optiplant.repository;

import com.example.optiplant.model.Sale;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, UUID> {

    boolean existsBySaleNumber(String saleNumber);
}
