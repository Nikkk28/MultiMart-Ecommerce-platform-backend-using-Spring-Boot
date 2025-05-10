package com.multimart.dto.common;

import com.multimart.model.Address;
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

    public static AddressDto fromEntity(Address address) {
        if (address == null) return null;
        return AddressDto.builder()
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .zipCode(address.getZipCode())
                .isDefault(address.isDefault())
                .build();
    }

}
