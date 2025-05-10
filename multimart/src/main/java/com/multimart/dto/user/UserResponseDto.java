package com.multimart.dto.user;

import com.multimart.dto.common.AddressDto;
import com.multimart.dto.vendor.VendorResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.management.relation.Role;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Role role;
    private AddressDto address;
    private VendorResponseDto vendor;
}
