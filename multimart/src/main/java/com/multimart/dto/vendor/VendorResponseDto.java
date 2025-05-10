package com.multimart.dto.vendor;

import com.multimart.dto.common.AddressDto;
import com.multimart.model.Vendor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorResponseDto {
    private String storeName;
    private String storeDescription;
    private String specialty;
    private AddressDto storeAddress;
    private String logo;
    private Vendor.ApprovalStatus approvalStatus;
}
