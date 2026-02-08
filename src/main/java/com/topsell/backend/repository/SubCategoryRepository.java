package com.topsell.backend.repository;

import com.topsell.backend.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {
    Optional<SubCategory> findBySlug(String slug);
    List<SubCategory> findByCategoryId(Long categoryId);
    void deleteByCategoryId(Long categoryId);
}
