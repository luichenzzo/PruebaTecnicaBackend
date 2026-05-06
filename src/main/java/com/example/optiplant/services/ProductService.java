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

/**
 * Coordinates product catalog reads and administrative product mutations.
 */
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

    /**
     * Lists all products.
     *
     * @return product responses
     */
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(ProductResponse::from).toList();
    }

    /**
     * Finds a product by identifier.
     *
     * @param id product identifier
     * @return product response
     */
    public ProductResponse findById(UUID id) {
        return ProductResponse.from(getProduct(id));
    }

    /**
     * Creates a product and broadcasts a realtime event.
     *
     * @param request product data
     * @return created product response
     */
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

    /**
     * Updates a product and broadcasts a realtime event.
     *
     * @param id product identifier
     * @param request product data
     * @return updated product response
     */
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

    /**
     * Deletes a product and broadcasts a realtime event.
     *
     * @param id product identifier
     */
    @Transactional
    public void delete(UUID id) {
        productRepository.delete(getProduct(id));
        realtimeNotificationService.productDeleted(id);
    }

    /**
     * Finds a product entity by identifier.
     *
     * @param id product identifier
     * @return matching product entity
     */
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
