package com.multimart.controller;

import com.multimart.dto.common.ApiResponse;
import com.multimart.dto.order.OrderDto;
import com.multimart.dto.order.OrderRequestDto;
import com.multimart.dto.order.OrderSummaryDto;
import com.multimart.model.Order;
import com.multimart.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<Page<OrderSummaryDto>> getUserOrders(
            @RequestParam(required = false) Order.OrderStatus status,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        return ResponseEntity.ok(orderService.getUserOrders(userDetails.getUsername(), status, pageable));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrderDetails(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        return ResponseEntity.ok(orderService.getOrderDetails(orderId, userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<OrderSummaryDto> createOrder(
            @RequestBody OrderRequestDto orderRequestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        return ResponseEntity.ok(orderService.createOrder(orderRequestDto, userDetails.getUsername()));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        orderService.cancelOrder(orderId, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse(true, "Order cancelled successfully"));
    }
}
