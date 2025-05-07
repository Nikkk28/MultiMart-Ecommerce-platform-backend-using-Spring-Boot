package com.multimart.controller;

import com.multimart.dto.common.ApiResponse;
import com.multimart.dto.wishlist.WishlistItemDto;
import com.multimart.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<List<WishlistItemDto>> getWishlist(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(wishlistService.getWishlist(userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> addToWishlist(
            @RequestBody WishlistItemDto wishlistItemDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        wishlistService.addToWishlist(userDetails.getUsername(), wishlistItemDto.getProductId());
        return ResponseEntity.ok(new ApiResponse(true, "Item added to wishlist"));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse> removeFromWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        wishlistService.removeFromWishlist(userDetails.getUsername(), productId);
        return ResponseEntity.ok(new ApiResponse(true, "Item removed from wishlist"));
    }
}
