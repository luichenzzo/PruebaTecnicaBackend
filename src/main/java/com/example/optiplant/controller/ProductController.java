package com.example.optiplant.controller;

import com.example.optiplant.dto.ProductRequest;
import com.example.optiplant.dto.ProductResponse;
import com.example.optiplant.services.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for product catalog operations.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Lists all products.
     *
     * @return product summaries
     */
    @GetMapping
    public List<ProductResponse> findAll() {
        return productService.findAll();
    }

    /**
     * Finds a product by identifier.
     *
     * @param id product identifier
     * @return matching product
     */
    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable UUID id) {
        return productService.findById(id);
    }

    /**
     * Creates a product.
     *
     * @param request validated product payload
     * @return created product
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    /**
     * Updates a product.
     *
     * @param id product identifier
     * @param request validated product payload
     * @return updated product
     */
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    /**
     * Deletes a product.
     *
     * @param id product identifier
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        productService.delete(id);
    }
}
