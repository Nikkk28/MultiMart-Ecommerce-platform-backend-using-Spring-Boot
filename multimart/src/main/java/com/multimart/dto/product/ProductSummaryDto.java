package com.multimart.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductSummaryDto {
    private Long id;
    private String name;
    private Double price;
    private Double originalPrice;
    private String image;
    private Double rating;
    private Integer reviewCount;
    private boolean isOnSale;
    private Double discountPercentage;
    private VendorSummaryDto vendor;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VendorSummaryDto {
        private Long id;
        private String name;
    }
}
