package com.multimart.dto.admin;

import com.multimart.dto.vendor.VendorSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardDto {
    private int userCount;
    private int newUsersThisMonth;
    private int vendorCount;
    private int pendingVendorCount;
    private int productCount;
    private int newProductsThisMonth;
    private int orderCount;
    private int newOrdersThisMonth;
    private double totalRevenue;
    private double monthlyRevenue;
    private int categoryCount;
    private List<VendorSummaryDto> pendingVendors;
}
