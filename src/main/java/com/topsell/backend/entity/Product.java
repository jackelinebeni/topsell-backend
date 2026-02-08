package com.topsell.backend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @Column(nullable = false)
    private String imageUrl;

    private boolean featured = false;
    private boolean active = true;

    // Lista de características del producto
    @ElementCollection
    @CollectionTable(name = "product_features", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "feature")
    private List<String> features = new ArrayList<>();

    // RELACIÓN: Muchos productos pertenecen a una Categoría
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // RELACIÓN: Muchos productos pertenecen a una Marca
    @ManyToOne
    @JoinColumn(name = "brand_id") // Puede ser nullable si hay productos sin marca (genéricos)
    private Brand brand;

    // RELACIÓN: Muchos productos pertenecen a una SubCategoría
    @ManyToOne
    @JoinColumn(name = "subcategory_id") // Puede ser nullable
    private SubCategory subCategory;

    // RELACIÓN: Un producto tiene múltiples imágenes secundarias
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<ProductImage> images = new ArrayList<>();
}