package com.example.optiplant.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Product catalog entity with supplier, unit of measure, and default costing
 * metadata.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
public class Product extends BaseEntity {

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_of_measure_id")
    private UnitOfMeasure unitOfMeasure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "default_cost", precision = 19, scale = 4)
    private BigDecimal defaultCost;

    @OneToMany(mappedBy = "product")
    private List<Inventory> inventories = new ArrayList<>();


}

