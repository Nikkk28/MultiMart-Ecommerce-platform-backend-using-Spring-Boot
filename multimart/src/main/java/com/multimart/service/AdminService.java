package com.multimart.service;

import com.multimart.dto.admin.AdminDashboardDto;
import com.multimart.dto.category.CategoryDto;
import com.multimart.dto.category.SubcategoryDto;
import com.multimart.dto.common.AddressDto;
import com.multimart.dto.user.UserDto;
import com.multimart.dto.user.UserStatusUpdateRequest;
import com.multimart.dto.vendor.VendorDto;
import com.multimart.dto.vendor.VendorSummaryDto;
import com.multimart.exception.ResourceNotFoundException;
import com.multimart.model.*;
import com.multimart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public AdminDashboardDto getAdminDashboard() {
        // Get user counts
        int userCount = (int) userRepository.count();
        int newUsersThisMonth = countUsersCreatedInMonth(YearMonth.now());

        // Get vendor counts
        int vendorCount = (int) vendorRepository.count();
        int pendingVendorCount = countVendorsByStatus(Vendor.ApprovalStatus.PENDING);

        // Get product counts
        int productCount = (int) productRepository.count();
        int newProductsThisMonth = countProductsCreatedInMonth(YearMonth.now());

        // Get order counts
        int orderCount = (int) orderRepository.count();
        int newOrdersThisMonth = countOrdersCreatedInMonth(YearMonth.now());

        // Get revenue
        double totalRevenue = calculateTotalRevenue();
        double monthlyRevenue = calculateMonthlyRevenue(YearMonth.now());

        // Get category count
        int categoryCount = (int) categoryRepository.count();

        // Get pending vendors
        List<VendorSummaryDto> pendingVendors = getPendingVendors();

        return AdminDashboardDto.builder()
                .userCount(userCount)
                .newUsersThisMonth(newUsersThisMonth)
                .vendorCount(vendorCount)
                .pendingVendorCount(pendingVendorCount)
                .productCount(productCount)
                .newProductsThisMonth(newProductsThisMonth)
                .orderCount(orderCount)
                .newOrdersThisMonth(newOrdersThisMonth)
                .totalRevenue(totalRevenue)
                .monthlyRevenue(monthlyRevenue)
                .categoryCount(categoryCount)
                .pendingVendors(pendingVendors)
                .build();
    }

    private int countUsersCreatedInMonth(YearMonth yearMonth) {
        // In a real implementation, this would query the database for users created in the specified month
        // For now, we'll return a placeholder value
        return 86;
    }

    private int countVendorsByStatus(Vendor.ApprovalStatus status) {
        // In a real implementation, this would query the database for vendors with the specified status
        // For now, we'll return a placeholder value
        return status == Vendor.ApprovalStatus.PENDING ? 2 : 0;
    }

    private int countProductsCreatedInMonth(YearMonth yearMonth) {
        // In a real implementation, this would query the database for products created in the specified month
        // For now, we'll return a placeholder value
        return 350;
    }

    private int countOrdersCreatedInMonth(YearMonth yearMonth) {
        // In a real implementation, this would query the database for orders created in the specified month
        // For now, we'll return a placeholder value
        return 42;
    }

    private double calculateTotalRevenue() {
        // In a real implementation, this would calculate the total revenue from all completed orders
        // For now, we'll return a placeholder value
        return 1245678.0;
    }

    private double calculateMonthlyRevenue(YearMonth yearMonth) {
        // In a real implementation, this would calculate the revenue for the specified month
        // For now, we'll return a placeholder value
        return 124567.0;
    }

    private List<VendorSummaryDto> getPendingVendors() {
        // In a real implementation, this would query the database for pending vendors
        // For now, we'll return a placeholder list
        return vendorRepository.findAll().stream()
                .filter(vendor -> vendor.getApprovalStatus() == Vendor.ApprovalStatus.PENDING)
                .map(this::mapToVendorSummaryDto)
                .collect(Collectors.toList());
    }

    private VendorSummaryDto mapToVendorSummaryDto(Vendor vendor) {
        return VendorSummaryDto.builder()
                .id(vendor.getId())
                .userId(vendor.getUser().getId())
                .storeName(vendor.getStoreName())
                .storeDescription(vendor.getStoreDescription())
                .city(vendor.getStoreAddress() != null ? vendor.getStoreAddress().getCity() : null)
                .state(vendor.getStoreAddress() != null ? vendor.getStoreAddress().getState() : null)
                .appliedDate(vendor.getJoinedDate())
                .build();
    }

    public Page<VendorDto> getAllVendors(Vendor.ApprovalStatus status, Pageable pageable) {
        Page<Vendor> vendors;

        if (status != null) {
            vendors = vendorRepository.findAll(pageable); // In a real implementation, filter by status
        } else {
            vendors = vendorRepository.findAll(pageable);
        }

        return vendors.map(this::mapToVendorDto);
    }

    public VendorDto getVendorById(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        return mapToVendorDto(vendor);
    }

    @Transactional
    public void approveVendor(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        vendor.setApprovalStatus(Vendor.ApprovalStatus.APPROVED);
        vendorRepository.save(vendor);
    }

    @Transactional
    public void rejectVendor(Long vendorId, String reason) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        vendor.setApprovalStatus(Vendor.ApprovalStatus.REJECTED);
        vendor.setRejectionReason(reason);
        vendorRepository.save(vendor);
    }

    // Category Management Methods

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        return mapToCategoryDto(category);
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        // Check if slug already exists
        if (categoryRepository.findBySlug(categoryDto.getSlug()).isPresent()) {
            throw new IllegalArgumentException("Category with slug '" + categoryDto.getSlug() + "' already exists");
        }

        Category category = Category.builder()
                .name(categoryDto.getName())
                .slug(categoryDto.getSlug())
                .image(categoryDto.getImage())
                .description(categoryDto.getDescription())
                .productCount(0)
                .featured(categoryDto.isFeatured())
                .build();

        Category savedCategory = categoryRepository.save(category);

        return mapToCategoryDto(savedCategory);
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check if slug already exists and belongs to another category
        categoryRepository.findBySlug(categoryDto.getSlug())
                .ifPresent(existingCategory -> {
                    if (!existingCategory.getId().equals(id)) {
                        throw new IllegalArgumentException("Category with slug '" + categoryDto.getSlug() + "' already exists");
                    }
                });

        category.setName(categoryDto.getName());
        category.setSlug(categoryDto.getSlug());
        category.setImage(categoryDto.getImage());
        category.setDescription(categoryDto.getDescription());
        category.setFeatured(categoryDto.isFeatured());

        Category updatedCategory = categoryRepository.save(category);

        return mapToCategoryDto(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check if category has products
        if (productRepository.findByCategory(category, Pageable.unpaged()).hasContent()) {
            throw new IllegalArgumentException("Cannot delete category with associated products");
        }

        categoryRepository.delete(category);
    }

    // User Management Methods

    public Page<UserDto> getAllUsers(String role, Pageable pageable) {
        Page<User> users;

        if (role != null) {
            Role roleEnum = Role.valueOf(role.toUpperCase());
            users = userRepository.findAll(pageable); // In a real implementation, filter by role
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(this::mapToUserDto);
    }

    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToUserDto(user);
    }

    @Transactional
    public void updateUserStatus(Long userId, UserStatusUpdateRequest.UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update user status based on the requested status
        switch (status) {
            case ACTIVE:
                user.setEnabled(true);
                user.setAccountNonLocked(true);
                break;
            case INACTIVE:
                user.setEnabled(false);
                user.setAccountNonLocked(true);
                break;
            case SUSPENDED:
                user.setEnabled(true);
                user.setAccountNonLocked(false);
                break;
        }

        userRepository.save(user);
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

    private CategoryDto mapToCategoryDto(Category category) {
        List<SubcategoryDto> subcategoryDtos = category.getSubcategories().stream()
                .map(this::mapToSubcategoryDto)
                .collect(Collectors.toList());

        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .image(category.getImage())
                .description(category.getDescription())
                .productCount(category.getProductCount())
                .featured(category.isFeatured())
                .subcategories(subcategoryDtos)
                .build();
    }

    private SubcategoryDto mapToSubcategoryDto(Subcategory subcategory) {
        return SubcategoryDto.builder()
                .id(subcategory.getId())
                .name(subcategory.getName())
                .slug(subcategory.getSlug())
                .productCount(subcategory.getProductCount())
                .build();
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .address(mapToAddressDto(user.getAddress()))
                .build();
    }
}
