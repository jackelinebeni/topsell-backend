package com.topsell.backend.controller;

import com.topsell.backend.entity.Brand;
import com.topsell.backend.repository.BrandRepository;
import com.topsell.backend.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    // ========== ENDPOINTS PÚBLICOS (TIENDA) ==========
    
    @GetMapping
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    // ========== ENDPOINTS ADMIN ==========
    
    @GetMapping("/admin")
    public List<Brand> getAllBrandsAdmin() {
        return brandRepository.findAll();
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<Brand> getBrandById(@PathVariable Long id) {
        return brandRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/admin")
    public Brand createBrand(@RequestBody Brand brand) {
        return brandRepository.save(brand);
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<Brand> updateBrand(@PathVariable Long id, @RequestBody Brand brandDetails) {
        return brandRepository.findById(id)
                .map(brand -> {
                    try {
                        // Si la URL del logo cambió, eliminar el anterior de Cloudinary
                        if (brandDetails.getLogoUrl() != null && 
                            !brandDetails.getLogoUrl().equals(brand.getLogoUrl()) &&
                            brand.getLogoUrl() != null && !brand.getLogoUrl().isEmpty()) {
                            cloudinaryService.deleteImage(brand.getLogoUrl());
                        }
                    } catch (Exception e) {
                        // Continuar aunque falle la eliminación de Cloudinary
                    }
                    
                    if (brandDetails.getName() != null) {
                        brand.setName(brandDetails.getName());
                    }
                    if (brandDetails.getLogoUrl() != null) {
                        brand.setLogoUrl(brandDetails.getLogoUrl());
                    }
                    if (brandDetails.getSortOrder() != null) {
                        brand.setSortOrder(brandDetails.getSortOrder());
                    }

                    if(brandDetails.getSlug() != null) {
                        brand.setSlug(brandDetails.getSlug());
                    }

                    return ResponseEntity.ok(brandRepository.save(brand));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteBrand(@PathVariable Long id) {
        return brandRepository.findById(id)
                .map(brand -> {
                    try {
                        // Eliminar logo de Cloudinary
                        if (brand.getLogoUrl() != null && !brand.getLogoUrl().isEmpty()) {
                            cloudinaryService.deleteImage(brand.getLogoUrl());
                        }
                        
                        // Eliminar la marca de la base de datos
                        brandRepository.delete(brand);
                        return ResponseEntity.ok().build();
                    } catch (Exception e) {
                        return ResponseEntity.status(500)
                                .body("Error al eliminar marca: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
}