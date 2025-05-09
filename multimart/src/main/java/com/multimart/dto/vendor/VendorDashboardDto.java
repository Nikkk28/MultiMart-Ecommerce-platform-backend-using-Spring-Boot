package com.multimart.dto.vendor;

import com.multimart.dto.order.OrderSummaryDto;
import com.multimart.dto.product.ProductSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorDashboardDto {
    private VendorDto vendorProfile;
    private int productCount;
    private List<ProductSummaryDto> recentProducts;
    private int orderCount;
    private List<OrderSummaryDto> recentOrders;
    private double totalRevenue;
    private double monthlyRevenue;
    private double previousMonthRevenue;
}
