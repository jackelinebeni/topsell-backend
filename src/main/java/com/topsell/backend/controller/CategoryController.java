package com.topsell.backend.controller;

import com.topsell.backend.entity.Brand;
import com.topsell.backend.entity.Category;
import com.topsell.backend.entity.SubCategory;
import com.topsell.backend.repository.CategoryRepository;
import com.topsell.backend.repository.SubCategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // ========== ENDPOINTS PÚBLICOS (TIENDA) ==========
    
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @GetMapping("/{slug}")
    public Category getCategoryBySlug(@PathVariable String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + slug));
    }

    // ========== ENDPOINTS ADMIN ==========
    
    @GetMapping("/admin")
    public List<Category> getAllCategoriesAdmin() {
        return categoryRepository.findAll();
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/admin")
    public Category createCategory(@RequestBody Category category) {
        // Guardar la categoría
        Category savedCategory = categoryRepository.save(category);
        
        // Guardar las subcategorías si existen
        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            for (SubCategory subCategory : category.getSubCategories()) {
                subCategory.setCategory(savedCategory);
            }
            subCategoryRepository.saveAll(category.getSubCategories());
        }
        
        return savedCategory;
    }

    @PutMapping("/admin/{id}")
    @Transactional
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails) {
        return categoryRepository.findById(id)
                .map(category -> {
                    category.setName(categoryDetails.getName());
                    category.setSlug(categoryDetails.getSlug());
                    category.setDescription(categoryDetails.getDescription());
                    category.setImage(categoryDetails.getImage());
                    
                    // Manejar subcategorías de manera explícita
                    if (categoryDetails.getSubCategories() != null) {
                        // Obtener las subcategorías actuales de la BD
                        List<SubCategory> currentSubs = subCategoryRepository.findByCategoryId(id);
                        
                        // Crear lista de IDs del request
                        List<Long> requestIds = categoryDetails.getSubCategories().stream()
                            .filter(sc -> sc.getId() != null)
                            .map(SubCategory::getId)
                            .toList();
                        
                        // Eliminar las subcategorías que ya no están en el request
                        for (SubCategory currentSub : currentSubs) {
                            if (!requestIds.contains(currentSub.getId())) {
                                subCategoryRepository.deleteById(currentSub.getId());
                            }
                        }
                        
                        // Forzar flush de eliminaciones
                        entityManager.flush();
                        
                        // Actualizar o crear subcategorías
                        for (SubCategory requestSubCategory : categoryDetails.getSubCategories()) {
                            if (requestSubCategory.getId() != null) {
                                // Actualizar existente - buscar la entidad manejada
                                SubCategory existing = entityManager.find(SubCategory.class, requestSubCategory.getId());
                                if (existing != null) {
                                    existing.setName(requestSubCategory.getName());
                                    existing.setSlug(requestSubCategory.getSlug());
                                    entityManager.merge(existing);
                                }
                            } else {
                                // Crear nueva
                                SubCategory newSub = new SubCategory();
                                newSub.setName(requestSubCategory.getName());
                                newSub.setSlug(requestSubCategory.getSlug());
                                newSub.setCategory(category);
                                entityManager.persist(newSub);
                            }
                        }
                    }
                    
                    // Flush antes de recargar
                    entityManager.flush();
                    
                    // Recargar la categoría con sus subcategorías actualizadas
                    entityManager.refresh(category);
                    return ResponseEntity.ok(category);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(category -> {
                    categoryRepository.delete(category);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}