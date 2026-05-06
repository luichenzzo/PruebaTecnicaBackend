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
 * Supplier entity used to group product sourcing metadata.
 */
@Entity
@Table(name = "suppliers")
@Getter
@Setter
public class Supplier extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "phone")
    private String phone;

    @OneToMany(mappedBy = "supplier")
    private List<Product> products = new ArrayList<>();


}

