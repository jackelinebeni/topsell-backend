package com.topsell.backend.repository;

import com.topsell.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Buscar por URL amigable
    Optional<Product> findBySlug(String slug);

    // Filtrar productos por categoría (usando el objeto Category o su ID)
    List<Product> findByCategoryId(Long categoryId);

    // Filtrar productos por subcategoría
    List<Product> findBySubCategoryId(Long subCategoryId);

    // Buscar productos por nombre (Buscador simple tipo "LIKE")
    // Ejemplo: Encuentra "Zapatilla Nike" si busco "Nike"
    List<Product> findByNameContainingIgnoreCase(String name);
}
