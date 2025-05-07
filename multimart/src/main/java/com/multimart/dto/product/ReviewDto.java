package com.multimart.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDto {
    private Long id;
    private UserSummaryDto user;
    private Integer rating;
    private String title;
    private String comment;
    private boolean verified;
    private Integer helpfulCount;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserSummaryDto {
        private Long id;
        private String name;
        private String username;
    }
}
