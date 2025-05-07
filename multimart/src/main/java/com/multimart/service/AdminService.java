package com.multimart.service;

import com.multimart.dto.common.AddressDto;
import com.multimart.dto.vendor.VendorDto;
import com.multimart.exception.ResourceNotFoundException;
import com.multimart.model.Vendor;
import com.multimart.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final VendorRepository vendorRepository;

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
}
