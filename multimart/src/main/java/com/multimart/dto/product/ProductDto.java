package com.multimart.dto.product;

import com.multimart.dto.category.CategoryDto;
import com.multimart.dto.category.SubcategoryDto;
import com.multimart.dto.vendor.VendorDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Double originalPrice;
    private List<String> images;
    private CategoryDto category;
    private SubcategoryDto subcategory;
    private VendorDto vendor;
    private Double rating;
    private Integer reviewCount;
    private Integer inventory;
    private List<SpecificationDto> specifications;
    private List<String> tags;
    private boolean isOnSale;
    private Double discountPercentage;
    private boolean inStock;
    private String sku;
    private Double weight;
    private DimensionsDto dimensions;
    private ShippingInfoDto shippingInfo;
    private List<ReviewDto> reviews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SpecificationDto {
        private String name;
        private String value;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DimensionsDto {
        private Double length;
        private Double width;
        private Double height;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShippingInfoDto {
        private boolean freeShipping;
        private String estimatedDelivery;
    }
}
