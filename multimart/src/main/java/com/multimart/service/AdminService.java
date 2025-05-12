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

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
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
        YearMonth now = YearMonth.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        return AdminDashboardDto.builder()
                .userCount(userRepository.countAll())
                .newUsersThisMonth(userRepository.countByCreatedAtMonth(month, year))
                .vendorCount(userRepository.countByRole(Role.VENDOR))
                .pendingVendorCount(vendorRepository.countByApprovalStatus(Vendor.ApprovalStatus.PENDING))
                .productCount(productRepository.countAll())
                .newProductsThisMonth(productRepository.countByCreatedAtMonth(month, year))
                .orderCount(orderRepository.countAll())
                .newOrdersThisMonth(orderRepository.countByCreatedAtMonth(month, year))
                .totalRevenue(Optional.ofNullable(orderRepository.getTotalRevenue()).orElse(0.0))
                .monthlyRevenue(Optional.ofNullable(orderRepository.getMonthlyRevenue(month, year)).orElse(0.0))
                .categoryCount(categoryRepository.countAll())
                .pendingVendors(vendorRepository.findByApprovalStatus(Vendor.ApprovalStatus.PENDING).stream()
                        .map(this::mapToVendorSummaryDto)
                        .collect(Collectors.toList()))
                .build();
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
        Page<Vendor> vendors = (status != null)
                ? vendorRepository.findByApprovalStatus(status, pageable)
                : vendorRepository.findAll(pageable);

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
        vendor.setProductCount(0);
        vendorRepository.save(vendor);
    }

    @Transactional
    public void rejectVendor(Long vendorId, String reason) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        vendor.setApprovalStatus(Vendor.ApprovalStatus.REJECTED);
        vendor.setRejectionReason(reason);
        vendor.setProductCount(0);
        vendorRepository.save(vendor);
    }

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
        categoryRepository.findBySlug(categoryDto.getSlug()).ifPresent(existing -> {
            throw new IllegalArgumentException("Category with slug '" + categoryDto.getSlug() + "' already exists");
        });

        Category category = Category.builder()
                .name(categoryDto.getName())
                .slug(categoryDto.getSlug())
                .image(categoryDto.getImage())
                .description(categoryDto.getDescription())
                .productCount(0)
                .featured(categoryDto.isFeatured())
                .build();

        return mapToCategoryDto(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        categoryRepository.findBySlug(categoryDto.getSlug())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new IllegalArgumentException("Category with slug '" + categoryDto.getSlug() + "' already exists");
                    }
                });

        category.setName(categoryDto.getName());
        category.setSlug(categoryDto.getSlug());
        category.setImage(categoryDto.getImage());
        category.setDescription(categoryDto.getDescription());
        category.setFeatured(categoryDto.isFeatured());

        return mapToCategoryDto(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (productRepository.findByCategory(category, Pageable.unpaged()).hasContent()) {
            throw new IllegalArgumentException("Cannot delete category with associated products");
        }

        categoryRepository.delete(category);
    }

    public Page<UserDto> getAllUsers(String role, Pageable pageable) {
        Page<User> users = (role != null)
                ? userRepository.findByRole(Role.valueOf(role.toUpperCase()), pageable)
                : userRepository.findAll(pageable);

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

        switch (status) {
            case ACTIVE -> {
                user.setEnabled(true);
                user.setAccountNonLocked(true);
            }
            case INACTIVE -> {
                user.setEnabled(false);
                user.setAccountNonLocked(true);
            }
            case SUSPENDED -> {
                user.setEnabled(true);
                user.setAccountNonLocked(false);
            }
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
        if (address == null) return null;

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