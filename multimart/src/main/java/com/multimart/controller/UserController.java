package com.multimart.controller;

import com.multimart.dto.common.ApiResponse;
import com.multimart.dto.user.ChangePasswordRequest;
import com.multimart.dto.user.UserDto;
import com.multimart.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserProfile(userDetails.getUsername()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateUserProfile(
            @RequestBody UserDto userDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.updateUserProfile(userDto, userDetails.getUsername()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.changePassword(request, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
    }
}
