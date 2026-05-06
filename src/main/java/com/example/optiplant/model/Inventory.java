package com.example.optiplant.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Current stock balance for a product at a branch.
 */
@Entity
@Table(name = "inventories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "branch_id"})
})
@Getter
@Setter
public class Inventory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "reserved", nullable = false, precision = 19, scale = 4)
    private BigDecimal reserved = BigDecimal.ZERO;

    @OneToMany(mappedBy = "inventory")
    private List<InventoryMovement> movements = new ArrayList<>();


}

