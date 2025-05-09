package com.multimart.dto.customer;

import com.multimart.model.UserAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerAddressDto {
    private Long id;
    private String country;
    private String state;
    private String city;
    private String zipCode;
    private String street;
    private boolean isDefault;
    private UserAddress.AddressLabel label;
}
