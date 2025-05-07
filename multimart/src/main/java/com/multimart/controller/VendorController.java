package com.multimart.controller;

import com.multimart.dto.common.ApiResponse;
import com.multimart.dto.product.ProductDto;
import com.multimart.dto.vendor.VendorDto;
import com.multimart.dto.vendor.VendorRegistrationDto;
import com.multimart.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerVendor(
            @RequestBody VendorRegistrationDto registrationDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        vendorService.registerVendor(registrationDto, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse(true, "Vendor registration successful"));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<VendorDto> getVendorProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(vendorService.getVendorProfile(userDetails.getUsername()));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<VendorDto> updateVendorProfile(
            @RequestBody VendorDto vendorDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        return ResponseEntity.ok(vendorService.updateVendorProfile(vendorDto, userDetails.getUsername()));
    }

    @GetMapping("/products")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Page<ProductDto>> getVendorProducts(
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        return ResponseEntity.ok(vendorService.getVendorProducts(userDetails.getUsername(), pageable));
    }

    @PostMapping("/products")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ProductDto> addProduct(
            @RequestBody ProductDto productDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        return ResponseEntity.ok(vendorService.addProduct(productDto, userDetails.getUsername()));
    }

    @PutMapping("/products/{productId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductDto productDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        ProductDto updatedProduct = vendorService.updateProduct(productId, productDto, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse(true, "Product updated successfully"));
    }

    @DeleteMapping("/products/{productId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse> deleteProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        vendorService.deleteProduct(productId, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse(true, "Product deleted successfully"));
    }
}
