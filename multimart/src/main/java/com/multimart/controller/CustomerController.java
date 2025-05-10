package com.multimart.controller;

import com.multimart.dto.common.ApiResponse;
import com.multimart.dto.customer.CustomerAddressDto;
import com.multimart.dto.customer.CustomerDashboardDto;
import com.multimart.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer  ")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/dashboard")
    public ResponseEntity<CustomerDashboardDto> getCustomerDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(customerService.getCustomerDashboard(userDetails.getUsername()));
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<CustomerAddressDto>> getCustomerAddresses(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(customerService.getCustomerAddresses(userDetails.getUsername()));
    }

    @PostMapping("/addresses")
    public ResponseEntity<CustomerAddressDto> addCustomerAddress(
            @RequestBody CustomerAddressDto addressDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(customerService.addCustomerAddress(addressDto, userDetails.getUsername()));
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<CustomerAddressDto> updateCustomerAddress(
            @PathVariable Long addressId,
            @RequestBody CustomerAddressDto addressDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(customerService.updateCustomerAddress(addressId, addressDto, userDetails.getUsername()));
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse> deleteCustomerAddress(
            @PathVariable Long addressId,
            @AuthenticationPrincipal UserDetails userDetails) {
        customerService.deleteCustomerAddress(addressId, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse(true, "Address deleted successfully"));
    }
}
