package com.multimart.dto.auth;

import com.multimart.dto.common.AddressDto;
import com.multimart.model.Address;
import com.multimart.model.Role;
import com.multimart.model.Vendor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9\\s-]{10,15}$", message = "Phone number should be valid")
    private String phoneNumber;
    private AddressDto address;
    private Role role;
    private AddressDto storeAddress;
    private String storeName;
    private String storeDescription;
    private String logo;
    private String specialty;
    private String rejectionReason;
    private Double rating;
    private Integer productCount;
    private LocalDateTime joinedDate= LocalDateTime.now();
    private Vendor.ApprovalStatus approvalStatus;

}
