package com.multimart.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDto {
    private List<OrderItemRequestDto> items;
    private Long shippingAddressId;
    private Long billingAddressId;
    private String paymentMethod;
    private String couponCode;
    private String notes;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemRequestDto {
        private Long productId;
        private Integer quantity;
    }
}
