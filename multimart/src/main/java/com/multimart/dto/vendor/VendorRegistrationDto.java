package com.multimart.dto.vendor;

import com.multimart.dto.common.AddressDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorRegistrationDto {
    private String storeName;
    private String storeDescription;
    private AddressDto storeAddress;
    private String specialty;
    private String contactEmail;
    private String contactPhone;
}
