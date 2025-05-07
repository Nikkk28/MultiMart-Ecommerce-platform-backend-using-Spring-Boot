package com.multimart.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDto {
    private Long id;
    private ProductDto product;
    private Integer quantity;
    private Double price;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductDto {
        private Long id;
        private String name;
        private String image;
        private Double price;
        private VendorDto vendor;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VendorDto {
        private Long id;
        private String name;
    }
}
