package com.multimart.service;

import com.multimart.dto.wishlist.WishlistItemDto;
import com.multimart.exception.ResourceNotFoundException;
import com.multimart.model.Product;
import com.multimart.model.User;
import com.multimart.model.WishlistItem;
import com.multimart.repository.ProductRepository;
import com.multimart.repository.UserRepository;
import com.multimart.repository.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public List<WishlistItemDto> getWishlist(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<WishlistItem> wishlistItems = wishlistItemRepository.findByUser(user);
        
        return wishlistItems.stream()
                .map(this::mapToWishlistItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addToWishlist(String username, Long productId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Check if product already in wishlist
        if (wishlistItemRepository.findByUserAndProduct(user, product).isPresent()) {
            throw new IllegalArgumentException("Product already in wishlist");
        }
        
        WishlistItem wishlistItem = WishlistItem.builder()
                .user(user)
                .product(product)
                .build();
        
        wishlistItemRepository.save(wishlistItem);
    }

    @Transactional
    public void removeFromWishlist(String username, Long productId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        WishlistItem wishlistItem = wishlistItemRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in wishlist"));
        
        wishlistItemRepository.delete(wishlistItem);
    }

    private WishlistItemDto mapToWishlistItemDto(WishlistItem wishlistItem) {
        return WishlistItemDto.builder()
                .id(wishlistItem.getId())
                .productId(wishlistItem.getProduct().getId())
                .product(WishlistItemDto.ProductDto.builder()
                        .id(wishlistItem.getProduct().getId())
                        .name(wishlistItem.getProduct().getName())
                        .image(wishlistItem.getProduct().getImages().isEmpty() ? null : wishlistItem.getProduct().getImages().get(0))
                        .price(wishlistItem.getProduct().getPrice())
                        .originalPrice(wishlistItem.getProduct().getOriginalPrice())
                        .vendor(WishlistItemDto.VendorDto.builder()
                                .id(wishlistItem.getProduct().getVendor().getId())
                                .name(wishlistItem.getProduct().getVendor().getStoreName())
                                .build())
                        .rating(wishlistItem.getProduct().getRating())
                        .build())
                .addedAt(wishlistItem.getAddedAt())
                .build();
    }
}
