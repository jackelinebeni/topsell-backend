package com.topsell.backend.controller;

import com.topsell.backend.entity.Banner;
import com.topsell.backend.repository.BannerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class BannerController {

    @Autowired
    private BannerRepository bannerRepository;

    @GetMapping
    public List<Banner> getActiveBanners() {
        // Retorna solo los activos y en orden (1, 2, 3...)
        return bannerRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    // --- MÉTODOS SOLO PARA ADMIN ---

    // Listar TODOS (incluyendo inactivos, para gestión)
    @GetMapping
    public List<Banner> getAllBanners() {
        return bannerRepository.findAll(); // Podrías ordenarlos por ID o displayOrder
    }

    @PostMapping
    public Banner createBanner(@RequestBody Banner banner) {
        if (banner.getSortOrder() == null) banner.setSortOrder(0);
        banner.setActive(banner.getActive());
        return bannerRepository.save(banner);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Banner> updateBanner(@PathVariable Long id, @RequestBody Banner details) {
        Banner banner = bannerRepository.findById(id).orElseThrow();
        banner.setTitle(details.getTitle());
        banner.setImageUrl(details.getImageUrl());
        banner.setTargetUrl(details.getTargetUrl());
        banner.setSortOrder(details.getSortOrder());
        banner.setActive(details.getActive());
        return ResponseEntity.ok(bannerRepository.save(banner));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable Long id) {
        bannerRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}