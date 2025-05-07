package com.multimart.controller;

import com.multimart.dto.cart.CartDto;
import com.multimart.dto.cart.CartItemDto;
import com.multimart.dto.common.ApiResponse;
import com.multimart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getUsername()));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse> addItemToCart(
            @RequestBody CartItemDto cartItemDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        cartService.addItemToCart(userDetails.getUsername(), cartItemDto);
        return ResponseEntity.ok(new ApiResponse(true, "Item added to cart"));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse> updateCartItem(
            @PathVariable Long itemId,
            @RequestBody CartItemDto cartItemDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        cartService.updateCartItem(userDetails.getUsername(), itemId, cartItemDto);
        return ResponseEntity.ok(new ApiResponse(true, "Cart item updated"));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse> removeItemFromCart(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        cartService.removeItemFromCart(userDetails.getUsername(), itemId);
        return ResponseEntity.ok(new ApiResponse(true, "Item removed from cart"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse(true, "Cart cleared"));
    }
}
