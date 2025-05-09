package com.multimart.dto.vendor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorSummaryDto {
    private Long id;
    private Long userId;
    private String storeName;
    private String storeDescription;
    private String city;
    private String state;
    private LocalDateTime appliedDate;
}
