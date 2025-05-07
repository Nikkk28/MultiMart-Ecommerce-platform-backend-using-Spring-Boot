package com.multimart.dto.cart;

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
public class CartDto {
    private Long id;
    private Long userId;
    private List<CartItemDto> items;
    private Integer totalItems;
    private Double subtotal;
    private Double tax;
    private Double shipping;
    private Double total;
    private String couponCode;
    private Double couponDiscount;
    private LocalDateTime updatedAt;
}
