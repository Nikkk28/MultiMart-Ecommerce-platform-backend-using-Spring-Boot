package com.multimart.service;

import com.multimart.dto.category.CategoryDto;
import com.multimart.dto.category.SubcategoryDto;
import com.multimart.dto.common.AddressDto;
import com.multimart.dto.order.OrderSummaryDto;
import com.multimart.dto.product.ProductDto;
import com.multimart.dto.product.ProductSummaryDto;
import com.multimart.dto.vendor.VendorDashboardDto;
import com.multimart.dto.vendor.VendorDto;
import com.multimart.exception.ResourceNotFoundException;
import com.multimart.model.*;
import com.multimart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public VendorDto getVendorProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        return mapToVendorDto(vendor);
    }

    @Transactional
    public VendorDto updateVendorProfile(VendorDto vendorDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        // Update vendor details
        vendor.setStoreName(vendorDto.getStoreName());
        vendor.setStoreDescription(vendorDto.getStoreDescription());
        vendor.setStoreAddress(mapToAddress(vendorDto.getStoreAddress()));
        vendor.setSpecialty(vendorDto.getSpecialty());
        vendor.setContactEmail(vendorDto.getContactEmail());
        vendor.setContactPhone(vendorDto.getContactPhone());

        Vendor updatedVendor = vendorRepository.save(vendor);

        return mapToVendorDto(updatedVendor);
    }

    public VendorDashboardDto getVendorDashboard(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        // Get vendor profile
        VendorDto vendorProfile = mapToVendorDto(vendor);

        // Get product count
        int productCount = vendor.getProductCount();

        // Get recent products
        List<ProductSummaryDto> recentProducts = productRepository.findByVendor(
                        vendor,
                        PageRequest.of(0, 5, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))
                )
                .map(this::mapToProductSummaryDto)
                .getContent();

        // Get order count and recent orders
        Page<Order> vendorOrders = orderRepository.findByVendor(
                vendor,
                PageRequest.of(0, 5, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))
        );

        int orderCount = vendorOrders.getTotalElements() > 0 ? (int) vendorOrders.getTotalElements() : 0;

        List<OrderSummaryDto> recentOrders = vendorOrders
                .map(orderService::mapToOrderSummaryDto)
                .getContent();

        // Calculate revenue metrics
        double totalRevenue = calculateTotalRevenue(vendor);
        double monthlyRevenue = calculateMonthlyRevenue(vendor, YearMonth.now());
        double previousMonthRevenue = calculateMonthlyRevenue(vendor, YearMonth.now().minusMonths(1));

        return VendorDashboardDto.builder()
                .vendorProfile(vendorProfile)
                .productCount(productCount)
                .recentProducts(recentProducts)
                .orderCount(orderCount)
                .recentOrders(recentOrders)
                .totalRevenue(totalRevenue)
                .monthlyRevenue(monthlyRevenue)
                .previousMonthRevenue(previousMonthRevenue)
                .build();
    }

    public Page<ProductDto> getVendorProducts(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        Page<Product> products = productRepository.findByVendor(vendor, pageable);

        return products.map(this::mapToProductDto);
    }

    @Transactional
    public ProductDto addProduct(ProductDto productDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        // Check if vendor is approved
        if (vendor.getApprovalStatus() != Vendor.ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Only approved vendors can add products. Your current status is: " + vendor.getApprovalStatus());
        }

        Category category = categoryRepository.findById(productDto.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Subcategory subcategory = null;
        if (productDto.getSubcategory() != null && productDto.getSubcategory().getId() != null) {
            subcategory = subcategoryRepository.findById(productDto.getSubcategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subcategory not found"));
        }

        // Create product
        Product product = Product.builder()
                .name(productDto.getName())
                .description(productDto.getDescription())
                .price(productDto.getPrice())
                .originalPrice(productDto.getOriginalPrice())
                .images(productDto.getImages())
                .category(category)
                .subcategory(subcategory)
                .vendor(vendor)
                .inventory(productDto.getInventory())
                .specifications(mapToSpecifications(productDto.getSpecifications()))
                .tags(productDto.getTags())
                .isOnSale(productDto.isOnSale())
                .discountPercentage(productDto.getDiscountPercentage())
                .inStock(productDto.getInventory() > 0)
                .sku(productDto.getSku())
                .weight(productDto.getWeight())
                .dimensions(mapToDimensions(productDto.getDimensions()))
                .shippingInfo(mapToShippingInfo(productDto.getShippingInfo()))
                .build();

        Product savedProduct = productRepository.save(product);

        // Update category and subcategory product counts
        category.setProductCount(category.getProductCount() + 1);
        categoryRepository.save(category);

        if (subcategory != null) {
            subcategory.setProductCount(subcategory.getProductCount() + 1);
            subcategoryRepository.save(subcategory);
        }

        // Update vendor product count
        vendor.setProductCount(vendor.getProductCount() + 1);
        vendorRepository.save(vendor);

        return mapToProductDto(savedProduct);
    }

    @Transactional
    public ProductDto updateProduct(Long productId, ProductDto productDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        // Check if vendor is approved
        if (vendor.getApprovalStatus() != Vendor.ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Only approved vendors can update products. Your current status is: " + vendor.getApprovalStatus());
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Ensure the product belongs to the vendor
        if (!product.getVendor().getId().equals(vendor.getId())) {
            throw new IllegalArgumentException("Product does not belong to vendor");
        }

        Category category = categoryRepository.findById(productDto.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Subcategory subcategory = null;
        if (productDto.getSubcategory() != null && productDto.getSubcategory().getId() != null) {
            subcategory = subcategoryRepository.findById(productDto.getSubcategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subcategory not found"));
        }

        // Update product
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setOriginalPrice(productDto.getOriginalPrice());
        product.setImages(productDto.getImages());

        // Handle category change
        if (!product.getCategory().getId().equals(category.getId())) {
            // Decrement old category product count
            Category oldCategory = product.getCategory();
            oldCategory.setProductCount(oldCategory.getProductCount() - 1);
            categoryRepository.save(oldCategory);

            // Increment new category product count
            category.setProductCount(category.getProductCount() + 1);
            categoryRepository.save(category);

            product.setCategory(category);
        }

        // Handle subcategory change
        if ((product.getSubcategory() == null && subcategory != null) ||
                (product.getSubcategory() != null && subcategory == null) ||
                (product.getSubcategory() != null && subcategory != null &&
                        !product.getSubcategory().getId().equals(subcategory.getId()))) {

            // Decrement old subcategory product count if exists
            if (product.getSubcategory() != null) {
                Subcategory oldSubcategory = product.getSubcategory();
                oldSubcategory.setProductCount(oldSubcategory.getProductCount() - 1);
                subcategoryRepository.save(oldSubcategory);
            }

            // Increment new subcategory product count if exists
            if (subcategory != null) {
                subcategory.setProductCount(subcategory.getProductCount() + 1);
                subcategoryRepository.save(subcategory);
            }

            product.setSubcategory(subcategory);
        }

        product.setInventory(productDto.getInventory());
        product.setSpecifications(mapToSpecifications(productDto.getSpecifications()));
        product.setTags(productDto.getTags());
        product.setOnSale(productDto.isOnSale());
        product.setDiscountPercentage(productDto.getDiscountPercentage());
        product.setInStock(productDto.getInventory() > 0);
        product.setSku(productDto.getSku());
        product.setWeight(productDto.getWeight());
        product.setDimensions(mapToDimensions(productDto.getDimensions()));
        product.setShippingInfo(mapToShippingInfo(productDto.getShippingInfo()));

        Product updatedProduct = productRepository.save(product);

        return mapToProductDto(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long productId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        // Check if vendor is approved
        if (vendor.getApprovalStatus() != Vendor.ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Only approved vendors can delete products. Your current status is: " + vendor.getApprovalStatus());
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Ensure the product belongs to the vendor
        if (!product.getVendor().getId().equals(vendor.getId())) {
            throw new IllegalArgumentException("Product does not belong to vendor");
        }

        // Update category and subcategory product counts
        Category category = product.getCategory();
        category.setProductCount(category.getProductCount() - 1);
        categoryRepository.save(category);

        if (product.getSubcategory() != null) {
            Subcategory subcategory = product.getSubcategory();
            subcategory.setProductCount(subcategory.getProductCount() - 1);
            subcategoryRepository.save(subcategory);
        }

        // Update vendor product count
        vendor.setProductCount(vendor.getProductCount() - 1);
        vendorRepository.save(vendor);

        // Delete product
        productRepository.delete(product);
    }

    public Page<OrderSummaryDto> getVendorOrders(String username, Order.OrderStatus status, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        // Check if vendor is approved
        if (vendor.getApprovalStatus() != Vendor.ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Only approved vendors can view orders. Your current status is: " + vendor.getApprovalStatus());
        }

        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByVendorAndStatus(vendor, status, pageable);
        } else {
            orders = orderRepository.findByVendor(vendor, pageable);
        }

        return orders.map(orderService::mapToOrderSummaryDto);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, Order.OrderStatus status, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        // Check if vendor is approved
        if (vendor.getApprovalStatus() != Vendor.ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Only approved vendors can update order status. Your current status is: " + vendor.getApprovalStatus());
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Ensure the order contains products from this vendor
        boolean hasVendorProducts = order.getItems().stream()
                .anyMatch(item -> item.getVendor().getId().equals(vendor.getId()));

        if (!hasVendorProducts) {
            throw new IllegalArgumentException("Order does not contain products from this vendor");
        }

        // Check if status transition is valid
        validateStatusTransition(order.getStatus(), status);

        order.setStatus(status);
        orderRepository.save(order);
    }

    private void validateStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        // Implement status transition validation logic
        // For example, can't go from DELIVERED to PROCESSING
        if (currentStatus == Order.OrderStatus.DELIVERED || currentStatus == Order.OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot change status of a delivered or cancelled order");
        }

        if (currentStatus == Order.OrderStatus.SHIPPED && newStatus == Order.OrderStatus.PROCESSING) {
            throw new IllegalArgumentException("Cannot change status from SHIPPED to PROCESSING");
        }
    }

    private double calculateTotalRevenue(Vendor vendor) {
        // In a real implementation, this would calculate the total revenue from all completed orders
        // For now, we'll return a placeholder value
        return 45231.0;
    }

    private double calculateMonthlyRevenue(Vendor vendor, YearMonth yearMonth) {
        // In a real implementation, this would calculate the revenue for the specified month
        // For now, we'll return placeholder values
        if (yearMonth.equals(YearMonth.now())) {
            return 12234.0;
        } else {
            return 10997.0;
        }
    }

    private Address mapToAddress(AddressDto addressDto) {
        if (addressDto == null) {
            return null;
        }

        return Address.builder()
                .country(addressDto.getCountry())
                .state(addressDto.getState())
                .city(addressDto.getCity())
                .zipCode(addressDto.getZipCode())
                .street(addressDto.getStreet())
                .isDefault(addressDto.isDefault())
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

    private VendorDto mapToVendorDto(Vendor vendor) {
        return VendorDto.builder()
                .id(vendor.getId())
                .userId(vendor.getUser().getId())
                .storeName(vendor.getStoreName())
                .storeDescription(vendor.getStoreDescription())
                .storeAddress(mapToAddressDto(vendor.getStoreAddress()))
                .logo(vendor.getLogo())
                .approvalStatus(vendor.getApprovalStatus())
                .rejectionReason(vendor.getRejectionReason())
                .rating(vendor.getRating())
                .productCount(vendor.getProductCount())
                .specialty(vendor.getSpecialty())
                .joinedDate(vendor.getJoinedDate())
                .contactEmail(vendor.getContactEmail())
                .contactPhone(vendor.getContactPhone())
                .build();
    }

    private List<Product.Specification> mapToSpecifications(List<ProductDto.SpecificationDto> specificationDtos) {
        if (specificationDtos == null) {
            return new ArrayList<>();
        }

        return specificationDtos.stream()
                .map(dto -> new Product.Specification(dto.getName(), dto.getValue()))
                .collect(Collectors.toList());
    }

    private Product.Dimensions mapToDimensions(ProductDto.DimensionsDto dimensionsDto) {
        if (dimensionsDto == null) {
            return null;
        }

        return new Product.Dimensions(
                dimensionsDto.getLength(),
                dimensionsDto.getWidth(),
                dimensionsDto.getHeight()
        );
    }

    private Product.ShippingInfo mapToShippingInfo(ProductDto.ShippingInfoDto shippingInfoDto) {
        if (shippingInfoDto == null) {
            return null;
        }

        return new Product.ShippingInfo(
                shippingInfoDto.isFreeShipping(),
                shippingInfoDto.getEstimatedDelivery()
        );
    }

    private ProductDto mapToProductDto(Product product) {
        List<ProductDto.SpecificationDto> specificationDtos = product.getSpecifications().stream()
                .map(spec -> ProductDto.SpecificationDto.builder()
                        .name(spec.getName())
                        .value(spec.getValue())
                        .build())
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
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
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
}