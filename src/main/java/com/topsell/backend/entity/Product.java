package com.topsell.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // "slug" para URLs amigables (ej: "polo-algodon-negro")
    @Column(unique = true)
    private String slug;

    @Column(length = 500)
    private String shortDescription;

    @Column(columnDefinition = "TEXT") // Permite textos muy largos
    private String longDescription;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(length = 500)
    private String image;

    // RELACIÓN: Muchos productos pertenecen a una Categoría
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // RELACIÓN: Muchos productos pertenecen a una Marca
    @ManyToOne
    @JoinColumn(name = "brand_id") // Puede ser nullable si hay productos sin marca (genéricos)
    private Brand brand;
}