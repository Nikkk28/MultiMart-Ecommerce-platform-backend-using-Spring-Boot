package com.multimart.dto.order;

import com.multimart.dto.common.AddressDto;
import com.multimart.model.Order;
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
public class OrderDto {
    private Long id;
    private String orderNumber;
    private Long userId;
    private List<OrderItemDto> items;
    private Order.OrderStatus status;
    private AddressDto shippingAddress;
    private AddressDto billingAddress;
    private String paymentMethod;
    private Order.PaymentStatus paymentStatus;
    private Double subtotal;
    private Double tax;
    private Double shipping;
    private Double discount;
    private Double total;
    private String couponCode;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
