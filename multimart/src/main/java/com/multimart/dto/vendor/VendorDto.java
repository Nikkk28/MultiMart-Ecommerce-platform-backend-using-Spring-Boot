package com.multimart.dto.vendor;

import com.multimart.dto.common.AddressDto;
import com.multimart.model.Vendor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorDto {
    private Long id;
    private Long userId;
    private String storeName;
    private String storeDescription;
    private AddressDto storeAddress;
    private String logo;
    private Vendor.ApprovalStatus approvalStatus;
    private String rejectionReason;
    private Double rating;
    private Integer productCount;
    private String specialty;
    private LocalDateTime joinedDate;
    private String contactEmail;
    private String contactPhone;
}
