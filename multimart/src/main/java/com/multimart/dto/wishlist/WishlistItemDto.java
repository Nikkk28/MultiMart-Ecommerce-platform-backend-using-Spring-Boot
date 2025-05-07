package com.multimart.dto.wishlist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WishlistItemDto {
    private Long id;
    private Long productId;
    private ProductDto product;
    private LocalDateTime addedAt;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductDto {
        private Long id;
        private String name;
        private String image;
        private Double price;
        private Double originalPrice;
        private VendorDto vendor;
        private Double rating;
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
