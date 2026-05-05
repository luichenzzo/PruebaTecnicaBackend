package com.example.optiplant.services;

import com.example.optiplant.dto.ProductRequest;
import com.example.optiplant.dto.ProductResponse;
import com.example.optiplant.exceptions.BadRequestException;
import com.example.optiplant.exceptions.NotFoundException;
import com.example.optiplant.model.Product;
import com.example.optiplant.repository.ProductRepository;
import com.example.optiplant.repository.SupplierRepository;
import com.example.optiplant.repository.UnitOfMeasureRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final SupplierRepository supplierRepository;
    private final RealtimeNotificationService realtimeNotificationService;

    public ProductService(
            ProductRepository productRepository,
            UnitOfMeasureRepository unitOfMeasureRepository,
            SupplierRepository supplierRepository,
            RealtimeNotificationService realtimeNotificationService
    ) {
        this.productRepository = productRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.supplierRepository = supplierRepository;
        this.realtimeNotificationService = realtimeNotificationService;
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(ProductResponse::from).toList();
    }

    public ProductResponse findById(UUID id) {
        return ProductResponse.from(getProduct(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        String sku = request.sku().trim();
        if (productRepository.existsBySku(sku)) {
            throw new BadRequestException("Product SKU is already registered");
        }

        Product product = new Product();
        apply(product, request);
        ProductResponse response = ProductResponse.from(productRepository.save(product));
        realtimeNotificationService.productCreated(response);
        return response;
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = getProduct(id);
        String sku = request.sku().trim();
        productRepository.findBySku(sku)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException("Product SKU is already registered");
                });

        apply(product, request);
        ProductResponse response = ProductResponse.from(productRepository.save(product));
        realtimeNotificationService.productUpdated(response);
        return response;
    }

    @Transactional
    public void delete(UUID id) {
        productRepository.delete(getProduct(id));
        realtimeNotificationService.productDeleted(id);
    }

    Product getProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    private void apply(Product product, ProductRequest request) {
        product.setSku(request.sku().trim());
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setDefaultCost(request.defaultCost());
        product.setUnitOfMeasure(request.unitOfMeasureId() == null ? null : unitOfMeasureRepository
                .findById(request.unitOfMeasureId())
                .orElseThrow(() -> new NotFoundException("Unit of measure not found")));
        product.setSupplier(request.supplierId() == null ? null : supplierRepository
                .findById(request.supplierId())
                .orElseThrow(() -> new NotFoundException("Supplier not found")));
    }
}
