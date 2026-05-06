package com.example.optiplant.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit of measure used to classify product quantities.
 */
@Entity
@Table(name = "unit_of_measure")
@Getter
@Setter
public class UnitOfMeasure extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "unitOfMeasure")
    private List<Product> products = new ArrayList<>();

}

