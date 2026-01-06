package com.topsell.backend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(unique = true)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String image;

    // --- AGREGAR ESTO ---
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @JsonManagedReference // Esto permite que se envíe la lista al frontend
    private List<SubCategory> subCategories;
}
