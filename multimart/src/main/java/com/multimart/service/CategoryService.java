package com.multimart.service;

import com.multimart.dto.category.CategoryDto;
import com.multimart.dto.category.SubcategoryDto;
import com.multimart.exception.ResourceNotFoundException;
import com.multimart.model.Category;
import com.multimart.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getCategoryByIdOrSlug(String idOrSlug) {
        Category category;
        try {
            Long id = Long.parseLong(idOrSlug);
            category = categoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        } catch (NumberFormatException e) {
            category = categoryRepository.findBySlug(idOrSlug)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + idOrSlug));
        }
        
        return mapToCategoryDto(category);
    }

    public List<CategoryDto> getFeaturedCategories() {
        return categoryRepository.findByFeaturedTrue().stream()
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    private CategoryDto mapToCategoryDto(Category category) {
        List<SubcategoryDto> subcategoryDtos = category.getSubcategories().stream()
                .map(subcategory -> SubcategoryDto.builder()
                        .id(subcategory.getId())
                        .name(subcategory.getName())
                        .slug(subcategory.getSlug())
                        .productCount(subcategory.getProductCount())
                        .build())
                .collect(Collectors.toList());
        
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .image(category.getImage())
                .description(category.getDescription())
                .productCount(category.getProductCount())
                .featured(category.isFeatured())
                .subcategories(subcategoryDtos)
                .build();
    }
}
