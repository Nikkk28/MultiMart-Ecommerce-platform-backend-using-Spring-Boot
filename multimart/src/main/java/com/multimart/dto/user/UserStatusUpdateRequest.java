package com.multimart.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStatusUpdateRequest {
    private UserStatus status;

    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED
    }
}
