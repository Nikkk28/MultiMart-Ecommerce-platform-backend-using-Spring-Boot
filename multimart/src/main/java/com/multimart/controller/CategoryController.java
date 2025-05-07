package com.multimart.controller;

import com.multimart.dto.category.CategoryDto;
import com.multimart.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{idOrSlug}")
    public ResponseEntity<CategoryDto> getCategoryByIdOrSlug(@PathVariable String idOrSlug) {
        return ResponseEntity.ok(categoryService.getCategoryByIdOrSlug(idOrSlug));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<CategoryDto>> getFeaturedCategories() {
        return ResponseEntity.ok(categoryService.getFeaturedCategories());
    }
}
