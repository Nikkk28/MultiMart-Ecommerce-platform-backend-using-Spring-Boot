package com.multimart.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubcategoryDto {
    private Long id;
    private String name;
    private String slug;
    private Integer productCount;
}
