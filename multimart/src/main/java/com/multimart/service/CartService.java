package com.multimart.service;

import com.multimart.dto.cart.CartDto;
import com.multimart.dto.cart.CartItemDto;
import com.multimart.exception.ResourceNotFoundException;
import com.multimart.model.Cart;
import com.multimart.model.CartItem;
import com.multimart.model.Product;
import com.multimart.model.User;
import com.multimart.repository.CartItemRepository;
import com.multimart.repository.CartRepository;
import com.multimart.repository.ProductRepository;
import com.multimart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final double TAX_RATE = 0.18; // 18% tax

    @Transactional
    public CartDto getCart(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
        
        return mapToCartDto(cart);
    }

    @Transactional
    public void addItemToCart(String username, CartItemDto cartItemDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Product product = productRepository.findById(cartItemDto.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
        
        // Check if product already in cart
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseGet(() -> {
                    CartItem newItem = CartItem.builder()
                            .cart(cart)
                            .product(product)
                            .quantity(0)
                            .price(product.getPrice())
                            .build();
                    return newItem;
                });
        
        cartItem.setQuantity(cartItem.getQuantity() + cartItemDto.getQuantity());
        cartItemRepository.save(cartItem);
        
        updateCartTotals(cart);
    }

    @Transactional
    public void updateCartItem(String username, Long itemId, CartItemDto cartItemDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        // Ensure the cart item belongs to the user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user's cart");
        }
        
        cartItem.setQuantity(cartItemDto.getQuantity());
        cartItemRepository.save(cartItem);
        
        updateCartTotals(cart);
    }

    @Transactional
    public void removeItemFromCart(String username, Long itemId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        // Ensure the cart item belongs to the user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user's cart");
        }
        
        cartItemRepository.delete(cartItem);
        
        updateCartTotals(cart);
    }

    @Transactional
    public void clearCart(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        cart.getItems().clear();
        cartRepository.save(cart);
        
        updateCartTotals(cart);
    }

    private void updateCartTotals(Cart cart) {
        List<CartItem> items = cart.getItems();
        
        int totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        
        double subtotal = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        double tax = subtotal * TAX_RATE;
        
        // Calculate shipping (free if subtotal > 1000, otherwise 100)
        double shipping = subtotal > 1000 ? 0 : 100;
        
        // Apply coupon discount if any
        double couponDiscount = 0;
        if (cart.getCouponCode() != null && !cart.getCouponCode().isEmpty()) {
            // In a real implementation, this would look up the coupon code and apply the discount
            couponDiscount = 0;
        }
        
        double total = subtotal + tax + shipping - couponDiscount;
        
        cart.setTotalItems(totalItems);
        cart.setSubtotal(subtotal);
        cart.setTax(tax);
        cart.setShipping(shipping);
        cart.setCouponDiscount(couponDiscount);
        cart.setTotal(total);
        
        cartRepository.save(cart);
    }

    private CartDto mapToCartDto(Cart cart) {
        List<CartItemDto> cartItemDtos = cart.getItems().stream()
                .map(this::mapToCartItemDto)
                .collect(Collectors.toList());
        
        return CartDto.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(cartItemDtos)
                .totalItems(cart.getTotalItems())
                .subtotal(cart.getSubtotal())
                .tax(cart.getTax())
                .shipping(cart.getShipping())
                .total(cart.getTotal())
                .couponCode(cart.getCouponCode())
                .couponDiscount(cart.getCouponDiscount())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemDto mapToCartItemDto(CartItem cartItem) {
        return CartItemDto.builder()
                .id(cartItem.getId())
                .product(CartItemDto.ProductDto.builder()
                        .id(cartItem.getProduct().getId())
                        .name(cartItem.getProduct().getName())
                        .image(cartItem.getProduct().getImages().isEmpty() ? null : cartItem.getProduct().getImages().get(0))
                        .price(cartItem.getProduct().getPrice())
                        .vendor(CartItemDto.VendorDto.builder()
                                .id(cartItem.getProduct().getVendor().getId())
                                .name(cartItem.getProduct().getVendor().getStoreName())
                                .build())
                        .build())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getPrice())
                .build();
    }
}
