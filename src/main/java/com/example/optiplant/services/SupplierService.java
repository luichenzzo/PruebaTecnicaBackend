package com.example.optiplant.services;

import com.example.optiplant.dto.SupplierResponse;
import com.example.optiplant.exceptions.NotFoundException;
import com.example.optiplant.model.Supplier;
import com.example.optiplant.repository.SupplierRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Provides read access to supplier data.
 */
@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    /**
     * Lists all suppliers.
     *
     * @return supplier responses
     */
    public List<SupplierResponse> findAll() {
        return supplierRepository.findAll().stream().map(SupplierResponse::from).toList();
    }

    /**
     * Finds a supplier by identifier.
     *
     * @param id supplier identifier
     * @return supplier response
     */
    public SupplierResponse findById(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier not found"));
        return SupplierResponse.from(supplier);
    }
}

