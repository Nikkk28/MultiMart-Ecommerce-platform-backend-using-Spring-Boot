package com.multimart.service;

import com.multimart.dto.category.CategoryDto;
import com.multimart.dto.category.SubcategoryDto;
import com.multimart.dto.common.AddressDto;
import com.multimart.dto.vendor.VendorDto;
import com.multimart.exception.ResourceNotFoundException;
import com.multimart.model.Category;
import com.multimart.model.Subcategory;
import com.multimart.model.Vendor;
import com.multimart.repository.CategoryRepository;
import com.multimart.repository.ProductRepository;
import com.multimart.repository.SubcategoryRepository;
import com.multimart.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final VendorRepository vendorRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final ProductRepository productRepository;

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

    private AddressDto mapToAddressDto(com.multimart.model.Address address) {
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
}
