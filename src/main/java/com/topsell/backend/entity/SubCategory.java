package com.topsell.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "subcategories")
public class SubCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String slug;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    @JsonBackReference // Evita bucles infinitos al convertir a JSON
    @EqualsAndHashCode.Exclude // Excluir de hashCode y equals para evitar bucle infinito
    @ToString.Exclude // Excluir de toString para evitar bucle infinito
    private Category category;
}