package com.topsell.backend.controller;

import com.topsell.backend.entity.Product;
import com.topsell.backend.entity.ProductImage;
import com.topsell.backend.repository.CategoryRepository;
import com.topsell.backend.repository.ProductRepository;
import com.topsell.backend.repository.ProductImageRepository;
import com.topsell.backend.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    // ========== ENDPOINTS PÚBLICOS (TIENDA) ==========
    
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Detalle de producto por URL (slug)
    @GetMapping("/{slug}")
    public Product getProductBySlug(@PathVariable String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    // Productos por Categoría (para los carruseles específicos de la Home)
    // Ejemplo: /api/products/category/ropa
    @GetMapping("/category/{categorySlug}")
    public List<Product> getProductsByCategory(@PathVariable String categorySlug) {
        Long categoryId = categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"))
                .getId();

        return productRepository.findByCategoryId(categoryId);
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String query) {
        return productRepository.findByNameContainingIgnoreCase(query);
    }

    // ========== ENDPOINTS ADMIN ==========
    
    @GetMapping("/admin")
    public List<Product> getAllProductsAdmin() {
        return productRepository.findAll();
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/admin")
    public Product createProduct(@RequestBody Product product) {
        // Guardar el producto
        Product savedProduct = productRepository.save(product);
        
        // Guardar las imágenes secundarias si existen
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            for (ProductImage image : product.getImages()) {
                image.setProduct(savedProduct);
            }
            productImageRepository.saveAll(product.getImages());
        }
        
        return savedProduct;
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        return productRepository.findById(id)
                .map(product -> {
                    try {
                        // Si la URL de la imagen principal cambió, eliminar la anterior de Cloudinary
                        if (productDetails.getImageUrl() != null && 
                            !productDetails.getImageUrl().equals(product.getImageUrl()) &&
                            product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                            cloudinaryService.deleteImage(product.getImageUrl());
                        }
                        
                        // Manejar imágenes secundarias
                        if (productDetails.getImages() != null) {
                            // Obtener URLs de las imágenes actuales
                            List<String> oldImageUrls = product.getImages().stream()
                                .map(ProductImage::getImageUrl)
                                .toList();
                            
                            // Obtener URLs de las nuevas imágenes
                            List<String> newImageUrls = productDetails.getImages().stream()
                                .map(ProductImage::getImageUrl)
                                .toList();
                            
                            // Eliminar de Cloudinary las imágenes que ya no están
                            for (String oldUrl : oldImageUrls) {
                                if (oldUrl != null && !oldUrl.isEmpty() && !newImageUrls.contains(oldUrl)) {
                                    cloudinaryService.deleteImage(oldUrl);
                                }
                            }
                            
                            // Limpiar y actualizar imágenes existentes
                            product.getImages().clear();
                            for (ProductImage image : productDetails.getImages()) {
                                image.setProduct(product);
                                product.getImages().add(image);
                            }
                        }
                    } catch (Exception e) {
                        // Continuar aunque falle la eliminación de Cloudinary
                    }
                    
                    product.setName(productDetails.getName());
                    product.setSlug(productDetails.getSlug());
                    product.setSku(productDetails.getSku());
                    product.setShortDescription(productDetails.getShortDescription());
                    product.setLongDescription(productDetails.getLongDescription());
                    product.setPrice(productDetails.getPrice());
                    product.setStock(productDetails.getStock());
                    product.setImageUrl(productDetails.getImageUrl());
                    product.setCategory(productDetails.getCategory());
                    product.setSubCategory(productDetails.getSubCategory());
                    product.setBrand(productDetails.getBrand());
                    product.setFeatures(productDetails.getFeatures());
                    product.setFeatured(productDetails.isFeatured());
                    product.setActive(productDetails.isActive());
                    
                    return ResponseEntity.ok(productRepository.save(product));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(product -> {
                    try {
                        // Eliminar imagen principal de Cloudinary
                        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                            cloudinaryService.deleteImage(product.getImageUrl());
                        }
                        
                        // Eliminar imágenes secundarias de Cloudinary
                        if (product.getImages() != null) {
                            for (ProductImage image : product.getImages()) {
                                if (image.getImageUrl() != null && !image.getImageUrl().isEmpty()) {
                                    cloudinaryService.deleteImage(image.getImageUrl());
                                }
                            }
                        }
                        
                        // Ahora eliminar el producto de la base de datos
                        productRepository.delete(product);
                        return ResponseEntity.ok().build();
                    } catch (Exception e) {
                        return ResponseEntity.status(500)
                                .body("Error al eliminar producto: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
}