package com.multimart.controller;

import com.multimart.dto.common.ApiResponse;
import com.multimart.dto.product.ProductDto;
import com.multimart.dto.product.ProductSummaryDto;
import com.multimart.dto.product.ReviewDto;
import com.multimart.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductSummaryDto>> getAllProducts(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Long vendor,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        
        return ResponseEntity.ok(productService.getAllProducts(
                minPrice, maxPrice, category, vendor, inStock, search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/trending")
    public ResponseEntity<List<ProductSummaryDto>> getTrendingProducts() {
        return ResponseEntity.ok(productService.getTrendingProducts());
    }

    @GetMapping("/recommended")
    public ResponseEntity<List<ProductSummaryDto>> getRecommendedProducts() {
        return ResponseEntity.ok(productService.getRecommendedProducts());
    }

    @GetMapping("/recently-viewed")
    public ResponseEntity<List<ProductSummaryDto>> getRecentlyViewedProducts(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(productService.getRecentlyViewedProducts(userDetails.getUsername()));
    }

    @GetMapping("/{productId}/similar")
    public ResponseEntity<List<ProductSummaryDto>> getSimilarProducts(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getSimilarProducts(productId));
    }

    @GetMapping("/{productId}/reviews")
    public ResponseEntity<List<ReviewDto>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductReviews(productId));
    }

    @PostMapping("/{productId}/reviews")
    public ResponseEntity<ReviewDto> addReview(
            @PathVariable Long productId,
            @RequestBody ReviewDto reviewDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        return ResponseEntity.ok(productService.addReview(productId, reviewDto, userDetails.getUsername()));
    }

    @PostMapping("/reviews/{reviewId}/helpful")
    public ResponseEntity<ApiResponse> markReviewAsHelpful(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        productService.markReviewAsHelpful(reviewId);
        return ResponseEntity.ok(new ApiResponse(true, "Review marked as helpful"));
    }
}
