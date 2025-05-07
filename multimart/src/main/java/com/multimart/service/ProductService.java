package com.multimart.service;

import com.multimart.dto.category.CategoryDto;
import com.multimart.dto.category.SubcategoryDto;
import com.multimart.dto.product.ProductDto;
import com.multimart.dto.product.ProductSummaryDto;
import com.multimart.dto.product.ReviewDto;
import com.multimart.dto.vendor.VendorDto;
import com.multimart.exception.ResourceNotFoundException;
import com.multimart.model.*;
import com.multimart.repository.ProductRepository;
import com.multimart.repository.ReviewRepository;
import com.multimart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public Page<ProductSummaryDto> getAllProducts(
            Double minPrice,
            Double maxPrice,
            Long categoryId,
            Long vendorId,
            Boolean inStock,
            String search,
            Pageable pageable) {
        
        Page<Product> products = productRepository.findWithFilters(
                minPrice, maxPrice, categoryId, vendorId, inStock, search, pageable);
        
        return products.map(this::mapToProductSummaryDto);
    }

    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        return mapToProductDto(product);
    }

    public List<ProductSummaryDto> getTrendingProducts() {
        return productRepository.findTrendingProducts(Pageable.ofSize(10)).stream()
                .map(this::mapToProductSummaryDto)
                .collect(Collectors.toList());
    }

    public List<ProductSummaryDto> getRecommendedProducts() {
        // In a real implementation, this would use a recommendation algorithm
        // For now, we'll just return trending products
        return getTrendingProducts();
    }

    public List<ProductSummaryDto> getRecentlyViewedProducts(String username) {
        // In a real implementation, this would track and return recently viewed products
        // For now, we'll just return trending products
        return getTrendingProducts();
    }

    public List<ProductSummaryDto> getSimilarProducts(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        // In a real implementation, this would find similar products based on category, tags, etc.
        // For now, we'll just return products from the same category
        return productRepository.findByCategory(product.getCategory(), Pageable.ofSize(10))
                .stream()
                .filter(p -> !p.getId().equals(productId))
                .map(this::mapToProductSummaryDto)
                .collect(Collectors.toList());
    }

    public List<ReviewDto> getProductReviews(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        return product.getReviews().stream()
                .map(this::mapToReviewDto)
                .collect(Collectors.toList());
    }

    public ReviewDto addReview(Long productId, ReviewDto reviewDto, String username) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if user already reviewed this product
        reviewRepository.findByUserAndProduct(user, product)
                .ifPresent(r -> {
                    throw new IllegalArgumentException("You have already reviewed this product");
                });
        
        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(reviewDto.getRating())
                .title(reviewDto.getTitle())
                .comment(reviewDto.getComment())
                .verified(true) // Assuming the user has purchased the product
                .helpfulCount(0)
                .build();
        
        Review savedReview = reviewRepository.save(review);
        
        // Update product rating
        updateProductRating(product);
        
        return mapToReviewDto(savedReview);
    }

    public void markReviewAsHelpful(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        reviewRepository.save(review);
    }

    private void updateProductRating(Product product) {
        List<Review> reviews = product.getReviews();
        if (reviews.isEmpty()) {
            product.setRating(0.0);
            product.setReviewCount(0);
        } else {
            double totalRating = reviews.stream()
                    .mapToDouble(Review::getRating)
                    .sum();
            product.setRating(totalRating / reviews.size());
            product.setReviewCount(reviews.size());
        }
        productRepository.save(product);
    }

    private ProductSummaryDto mapToProductSummaryDto(Product product) {
        return ProductSummaryDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .image(product.getImages().isEmpty() ? null : product.getImages().get(0))
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .isOnSale(product.isOnSale())
                .discountPercentage(product.getDiscountPercentage())
                .vendor(ProductSummaryDto.VendorSummaryDto.builder()
                        .id(product.getVendor().getId())
                        .name(product.getVendor().getStoreName())
                        .build())
                .build();
    }

    private ProductDto mapToProductDto(Product product) {
        List<ProductDto.SpecificationDto> specificationDtos = product.getSpecifications().stream()
                .map(spec -> ProductDto.SpecificationDto.builder()
                        .name(spec.getName())
                        .value(spec.getValue())
                        .build())
                .collect(Collectors.toList());
        
        List<ReviewDto> reviewDtos = product.getReviews().stream()
                .map(this::mapToReviewDto)
                .collect(Collectors.toList());
        
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .images(product.getImages())
                .category(CategoryDto.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .slug(product.getCategory().getSlug())
                        .build())
                .subcategory(product.getSubcategory() != null ? SubcategoryDto.builder()
                        .id(product.getSubcategory().getId())
                        .name(product.getSubcategory().getName())
                        .slug(product.getSubcategory().getSlug())
                        .build() : null)
                .vendor(VendorDto.builder()
                        .id(product.getVendor().getId())
                        .userId(product.getVendor().getUser().getId())
                        .storeName(product.getVendor().getStoreName())
                        .rating(product.getVendor().getRating())
                        .build())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .inventory(product.getInventory())
                .specifications(specificationDtos)
                .tags(product.getTags())
                .isOnSale(product.isOnSale())
                .discountPercentage(product.getDiscountPercentage())
                .inStock(product.isInStock())
                .sku(product.getSku())
                .weight(product.getWeight())
                .dimensions(product.getDimensions() != null ? ProductDto.DimensionsDto.builder()
                        .length(product.getDimensions().getLength())
                        .width(product.getDimensions().getWidth())
                        .height(product.getDimensions().getHeight())
                        .build() : null)
                .shippingInfo(product.getShippingInfo() != null ? ProductDto.ShippingInfoDto.builder()
                        .freeShipping(product.getShippingInfo().isFreeShipping())
                        .estimatedDelivery(product.getShippingInfo().getEstimatedDelivery())
                        .build() : null)
                .reviews(reviewDtos)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ReviewDto mapToReviewDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .user(ReviewDto.UserSummaryDto.builder()
                        .id(review.getUser().getId())
                        .name(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                        .username(review.getUser().getUsername())
                        .build())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .verified(review.isVerified())
                .helpfulCount(review.getHelpfulCount())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
