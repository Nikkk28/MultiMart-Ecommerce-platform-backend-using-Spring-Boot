package com.multimart.repository;

import com.multimart.model.Category;
import com.multimart.model.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {
    List<Subcategory> findByCategory(Category category);
    Optional<Subcategory> findBySlugAndCategory(String slug, Category category);
}
