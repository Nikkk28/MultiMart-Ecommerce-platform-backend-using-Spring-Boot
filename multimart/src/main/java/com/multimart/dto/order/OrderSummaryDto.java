package com.multimart.dto.order;

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
public class OrderSummaryDto {
    private Long id;
    private String orderNumber;
    private Order.OrderStatus status;
    private List<OrderItemDto> items;
    private Double subtotal;
    private Double tax;
    private Double shipping;
    private Double discount;
    private Double total;
    private LocalDateTime createdAt;
}
