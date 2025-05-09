package com.multimart.controller;

import com.multimart.dto.common.ApiResponse;
import com.multimart.dto.order.OrderSummaryDto;
import com.multimart.dto.product.ProductDto;
import com.multimart.dto.vendor.VendorDashboardDto;
import com.multimart.dto.vendor.VendorDto;
import com.multimart.model.Order;
import com.multimart.service.VendorService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<VendorDto> getVendorProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(vendorService.getVendorProfile(userDetails.getUsername()));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<VendorDto> updateVendorProfile(
            @RequestBody VendorDto vendorDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(vendorService.updateVendorProfile(vendorDto, userDetails.getUsername()));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<VendorDashboardDto> getVendorDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(vendorService.getVendorDashboard(userDetails.getUsername()));
    }

    @GetMapping("/products")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Page<ProductDto>> getVendorProducts(
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(vendorService.getVendorProducts(userDetails.getUsername(), pageable));
    }

    @PostMapping("/products")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ProductDto> addProduct(
            @RequestBody ProductDto productDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(vendorService.addProduct(productDto, userDetails.getUsername()));
    }

    @PutMapping("/products/{productId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductDto productDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        ProductDto updatedProduct = vendorService.updateProduct(productId, productDto, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse(true, "Product updated successfully"));
    }

    @DeleteMapping("/products/{productId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse> deleteProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {

        vendorService.deleteProduct(productId, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse(true, "Product deleted successfully"));
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Page<OrderSummaryDto>> getVendorOrders(
            @RequestParam(required = false) Order.OrderStatus status,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(vendorService.getVendorOrders(userDetails.getUsername(), status, pageable));
    }

    @PutMapping("/orders/{orderId}/status")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        vendorService.updateOrderStatus(orderId, request.getStatus(), userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse(true, "Order status updated successfully"));
    }

    @Data
    public static class OrderStatusUpdateRequest {
        private Order.OrderStatus status;
    }
}
