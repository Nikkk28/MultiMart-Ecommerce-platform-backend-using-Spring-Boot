package com.multimart.service;

import com.multimart.dto.common.AddressDto;
import com.multimart.dto.order.OrderDto;
import com.multimart.dto.order.OrderItemDto;
import com.multimart.dto.order.OrderRequestDto;
import com.multimart.dto.order.OrderSummaryDto;
import com.multimart.exception.ResourceNotFoundException;
import com.multimart.model.*;
import com.multimart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;

    private final double TAX_RATE = 0.18; // 18% tax

    public Page<OrderSummaryDto> getUserOrders(String username, Order.OrderStatus status, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByUserAndStatus(user, status, pageable);
        } else {
            orders = orderRepository.findByUser(user, pageable);
        }
        
        return orders.map(this::mapToOrderSummaryDto);
    }

    public OrderDto getOrderDetails(Long orderId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Ensure the order belongs to the user
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Order does not belong to user");
        }
        
        return mapToOrderDto(order);
    }

    @Transactional
    public OrderSummaryDto createOrder(OrderRequestDto orderRequestDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Create new order
        Order order = Order.builder()
                .user(user)
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status(Order.OrderStatus.PENDING)
                .paymentStatus(Order.PaymentStatus.PENDING)
                .shippingAddress(user.getAddress()) // In a real implementation, get from addressId
                .billingAddress(user.getAddress()) // In a real implementation, get from addressId
                .paymentMethod(orderRequestDto.getPaymentMethod())
                .couponCode(orderRequestDto.getCouponCode())
                .notes(orderRequestDto.getNotes())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        List<OrderItem> orderItems = new ArrayList<>();
        double subtotal = 0;
        
        // Process items from request
        if (orderRequestDto.getItems() != null && !orderRequestDto.getItems().isEmpty()) {
            for (OrderRequestDto.OrderItemRequestDto itemDto : orderRequestDto.getItems()) {
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
                
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .productName(product.getName())
                        .productImage(product.getImages().isEmpty() ? null : product.getImages().get(0))
                        .quantity(itemDto.getQuantity())
                        .price(product.getPrice())
                        .vendor(product.getVendor())
                        .vendorName(product.getVendor().getStoreName())
                        .build();
                
                orderItems.add(orderItem);
                subtotal += product.getPrice() * itemDto.getQuantity();
            }
        } else {
            // If no items in request, use cart items
            Cart cart = cartRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
            
            if (cart.getItems().isEmpty()) {
                throw new IllegalArgumentException("Cart is empty");
            }
            
            for (CartItem cartItem : cart.getItems()) {
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(cartItem.getProduct())
                        .productName(cartItem.getProduct().getName())
                        .productImage(cartItem.getProduct().getImages().isEmpty() ? null : cartItem.getProduct().getImages().get(0))
                        .quantity(cartItem.getQuantity())
                        .price(cartItem.getPrice())
                        .vendor(cartItem.getProduct().getVendor())
                        .vendorName(cartItem.getProduct().getVendor().getStoreName())
                        .build();
                
                orderItems.add(orderItem);
                subtotal += cartItem.getPrice() * cartItem.getQuantity();
            }
            
            // Clear cart after order is created
            cartService.clearCart(username);
        }
        
        order.setItems(orderItems);
        
        // Calculate totals
        double tax = subtotal * TAX_RATE;
        double shipping = subtotal > 1000 ? 0 : 100;
        double discount = 0;
        
        // Apply coupon if any
        if (order.getCouponCode() != null && !order.getCouponCode().isEmpty()) {
            // In a real implementation, this would look up the coupon code and apply the discount
            discount = 0;
        }
        
        double total = subtotal + tax + shipping - discount;
        
        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setShipping(shipping);
        order.setDiscount(discount);
        order.setTotal(total);
        
        Order savedOrder = orderRepository.save(order);
        
        return mapToOrderSummaryDto(savedOrder);
    }

    @Transactional
    public void cancelOrder(Long orderId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Ensure the order belongs to the user
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Order does not belong to user");
        }
        
        // Check if order can be cancelled
        if (order.getStatus() == Order.OrderStatus.DELIVERED || 
            order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Order cannot be cancelled");
        }
        
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    OrderSummaryDto mapToOrderSummaryDto(Order order) {
        List<OrderItemDto> orderItemDtos = order.getItems().stream()
                .map(this::mapToOrderItemDto)
                .collect(Collectors.toList());
        
        return OrderSummaryDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .items(orderItemDtos)
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .shipping(order.getShipping())
                .discount(order.getDiscount())
                .total(order.getTotal())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderDto mapToOrderDto(Order order) {
        List<OrderItemDto> orderItemDtos = order.getItems().stream()
                .map(this::mapToOrderItemDto)
                .collect(Collectors.toList());
        
        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .items(orderItemDtos)
                .status(order.getStatus())
                .shippingAddress(mapToAddressDto(order.getShippingAddress()))
                .billingAddress(mapToAddressDto(order.getBillingAddress()))
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .shipping(order.getShipping())
                .discount(order.getDiscount())
                .total(order.getTotal())
                .couponCode(order.getCouponCode())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemDto mapToOrderItemDto(OrderItem orderItem) {
        return OrderItemDto.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProduct().getId())
                .productName(orderItem.getProductName())
                .productImage(orderItem.getProductImage())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .vendorId(orderItem.getVendor().getId())
                .vendorName(orderItem.getVendorName())
                .build();
    }

    private AddressDto mapToAddressDto(Address address) {
        if (address == null) {
            return null;
        }
        
        return AddressDto.builder()
                .country(address.getCountry())
                .state(address.getState())
                .city(address.getCity())
                .zipCode(address.getZipCode())
                .street(address.getStreet())
                .isDefault(address.isDefault())
                .build();
    }
}
