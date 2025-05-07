package com.multimart.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    private Long id;
    private String name;
    private String slug;
    private String image;
    private String description;
    private Integer productCount;
    private boolean featured;
    private List<SubcategoryDto> subcategories;
}
