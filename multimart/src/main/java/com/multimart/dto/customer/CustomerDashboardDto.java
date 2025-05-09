package com.multimart.dto.customer;

import com.multimart.dto.order.OrderSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDashboardDto {
    private int orderCount;
    private List<OrderSummaryDto> recentOrders;
    private int cartItems;
    private double cartTotal;
    private int wishlistCount;
    private int savedAddressCount;
}
