package com.topsell.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Integer displayOrder = 0; // Orden de visualización de las imágenes

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Product product;
}
