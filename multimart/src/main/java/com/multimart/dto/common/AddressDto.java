package com.multimart.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressDto {
    private String country;
    private String state;
    private String city;
    private String zipCode;
    private String street;
    private boolean isDefault;
}
