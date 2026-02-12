package com.topsell.backend.controller;

import com.topsell.backend.entity.Brand;
import com.topsell.backend.entity.Category;
import com.topsell.backend.entity.SubCategory;
import com.topsell.backend.repository.CategoryRepository;
import com.topsell.backend.repository.ProductRepository;
import com.topsell.backend.repository.SubCategoryRepository;
import com.topsell.backend.service.CloudinaryService;
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

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ProductRepository productRepository;

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
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails) {
        return categoryRepository.findById(id)
                .map(category -> {
                    try {
                        // Si la URL de la imagen cambió, eliminar la anterior de Cloudinary
                        if (categoryDetails.getImage() != null && 
                            !categoryDetails.getImage().equals(category.getImage()) &&
                            category.getImage() != null && !category.getImage().isEmpty()) {
                            cloudinaryService.deleteImage(category.getImage());
                        }
                    } catch (Exception e) {
                        // Continuar aunque falle la eliminación de Cloudinary
                    }
                    
                    // Actualizar solo los campos que realmente cambiaron
                    if (categoryDetails.getName() != null && !categoryDetails.getName().equals(category.getName())) {
                        category.setName(categoryDetails.getName());
                    }
                    if (categoryDetails.getSlug() != null && !categoryDetails.getSlug().equals(category.getSlug())) {
                        category.setSlug(categoryDetails.getSlug());
                    }
                    if (categoryDetails.getDescription() != null && !categoryDetails.getDescription().equals(category.getDescription())) {
                        category.setDescription(categoryDetails.getDescription());
                    }
                    if (categoryDetails.getImage() != null && !categoryDetails.getImage().equals(category.getImage())) {
                        category.setImage(categoryDetails.getImage());
                    }
                    
                    try {
                        // SOLO manejar subcategorías si se enviaron explícitamente en el request
                        // Si el frontend no envía subcategorías (null), no las tocamos
                        // Si envía una lista vacía [], significa que quiere eliminarlas todas
                        if (categoryDetails.getSubCategories() != null && !categoryDetails.getSubCategories().isEmpty()) {
                            // Obtener las subcategorías actuales de la BD
                            List<SubCategory> currentSubs = subCategoryRepository.findByCategoryId(id);
                            
                            // Crear lista de IDs del request (para subcategorías existentes)
                            List<Long> requestIds = categoryDetails.getSubCategories().stream()
                                .filter(sc -> sc.getId() != null)
                                .map(SubCategory::getId)
                                .toList();
                            
                            // Crear lista de slugs del request (para identificar subcategorías por su slug)
                            List<String> requestSlugs = categoryDetails.getSubCategories().stream()
                                .filter(sc -> sc.getSlug() != null)
                                .map(SubCategory::getSlug)
                                .toList();
                            
                            // Eliminar las subcategorías que ya no están en el request
                            for (SubCategory currentSub : currentSubs) {
                                // Considerar que NO debe eliminarse si:
                                // 1. Su ID está en la lista de IDs del request, O
                                // 2. Su slug está en la lista de slugs del request (sin importar si tiene ID o no)
                                boolean shouldKeep = requestIds.contains(currentSub.getId()) || 
                                                   requestSlugs.contains(currentSub.getSlug());
                                
                                if (!shouldKeep) {
                                    // Verificar si hay productos asociados a esta subcategoría
                                    long productCount = productRepository.findBySubCategoryId(currentSub.getId()).size();
                                    if (productCount > 0) {
                                        throw new RuntimeException(
                                            "No se puede eliminar la subcategoría '" + currentSub.getName() + 
                                            "' porque tiene " + productCount + " producto(s) asociado(s). " +
                                            "Primero elimine o reasigne los productos a otra subcategoría.");
                                    }
                                    subCategoryRepository.deleteById(currentSub.getId());
                                }
                            }
                            
                            // Forzar flush de eliminaciones
                            entityManager.flush();
                            
                            // Actualizar o crear subcategorías
                            for (SubCategory requestSubCategory : categoryDetails.getSubCategories()) {
                                if (requestSubCategory.getId() != null) {
                                    // Actualizar existente por ID - buscar la entidad manejada
                                    SubCategory existing = entityManager.find(SubCategory.class, requestSubCategory.getId());
                                    if (existing != null) {
                                        existing.setName(requestSubCategory.getName());
                                        existing.setSlug(requestSubCategory.getSlug());
                                        entityManager.merge(existing);
                                    }
                                } else if (requestSubCategory.getSlug() != null) {
                                    // Buscar si ya existe por slug (para cuando el frontend no envía ID)
                                    SubCategory existing = currentSubs.stream()
                                        .filter(sub -> sub.getSlug().equals(requestSubCategory.getSlug()))
                                        .findFirst()
                                        .orElse(null);
                                    
                                    if (existing != null) {
                                        // Actualizar existente encontrado por slug
                                        existing.setName(requestSubCategory.getName());
                                        existing.setSlug(requestSubCategory.getSlug());
                                        entityManager.merge(existing);
                                    } else {
                                        // Crear nueva subcategoría
                                        SubCategory newSub = new SubCategory();
                                        newSub.setName(requestSubCategory.getName());
                                        newSub.setSlug(requestSubCategory.getSlug());
                                        newSub.setCategory(category);
                                        entityManager.persist(newSub);
                                    }
                                }
                            }
                        }
                        
                        // Guardar los cambios en la categoría
                        categoryRepository.save(category);
                        
                        // Recargar la categoría con sus subcategorías actualizadas
                        Category updatedCategory = categoryRepository.findById(id).orElse(category);
                        return ResponseEntity.ok(updatedCategory);
                        
                    } catch (RuntimeException e) {
                        // Capturar errores de validación de subcategorías con productos
                        return ResponseEntity.status(409).body(e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(category -> {
                    // Verificar si hay productos asociados a esta categoría
                    long productCount = productRepository.findByCategoryId(id).size();
                    
                    if (productCount > 0) {
                        return ResponseEntity.status(409) // 409 Conflict
                                .body("No se puede eliminar esta categoría porque tiene " + 
                                      productCount + " producto(s) asociado(s). " +
                                      "Primero elimine o reasigne los productos a otra categoría.");
                    }
                    
                    try {
                        // Eliminar imagen de Cloudinary
                        if (category.getImage() != null && !category.getImage().isEmpty()) {
                            cloudinaryService.deleteImage(category.getImage());
                        }
                        
                        // Eliminar la categoría de la base de datos
                        categoryRepository.delete(category);
                        return ResponseEntity.ok().build();
                    } catch (Exception e) {
                        return ResponseEntity.status(500)
                                .body("Error al eliminar categoría: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
}