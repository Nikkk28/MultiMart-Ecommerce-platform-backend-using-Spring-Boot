package com.multimart.controller;

import com.multimart.dto.admin.AdminDashboardDto;
import com.multimart.dto.category.CategoryDto;
import com.multimart.dto.common.ApiResponse;
import com.multimart.dto.user.UserDto;
import com.multimart.dto.user.UserStatusUpdateRequest;
import com.multimart.dto.vendor.VendorDto;
import com.multimart.model.Vendor;
import com.multimart.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDto> getAdminDashboard() {
        return ResponseEntity.ok(adminService.getAdminDashboard());
    }

    @GetMapping("/vendors")
    public ResponseEntity<Page<VendorDto>> getAllVendors(
            @RequestParam(required = false) Vendor.ApprovalStatus status,
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(adminService.getAllVendors(status, pageable));
    }

    @GetMapping("/vendors/{vendorId}")
    public ResponseEntity<VendorDto> getVendorById(@PathVariable Long vendorId) {
        return ResponseEntity.ok(adminService.getVendorById(vendorId));
    }

    @PostMapping("/vendors/{vendorId}/approve")
    public ResponseEntity<ApiResponse> approveVendor(@PathVariable Long vendorId) {
        adminService.approveVendor(vendorId);
        return ResponseEntity.ok(new ApiResponse(true, "Vendor approved successfully"));
    }

    @PostMapping("/vendors/{vendorId}/reject")
    public ResponseEntity<ApiResponse> rejectVendor(
            @PathVariable Long vendorId,
            @RequestParam String reason) {

        adminService.rejectVendor(vendorId, reason);
        return ResponseEntity.ok(new ApiResponse(true, "Vendor rejected successfully"));
    }

    // Category Management Endpoints

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(adminService.getAllCategories());
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getCategoryById(id));
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto categoryDto) {
        return ResponseEntity.ok(adminService.createCategory(categoryDto));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryDto categoryDto) {
        return ResponseEntity.ok(adminService.updateCategory(id, categoryDto));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return ResponseEntity.ok(new ApiResponse(true, "Category deleted successfully"));
    }

    // User Management Endpoints

    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(required = false) String role,
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(adminService.getAllUsers(role, pageable));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUserById(userId));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UserStatusUpdateRequest request) {

        adminService.updateUserStatus(userId, request.getStatus());
        return ResponseEntity.ok(new ApiResponse(true, "User status updated successfully"));
    }
}
