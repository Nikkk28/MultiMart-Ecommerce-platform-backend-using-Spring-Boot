package com.multimart.service;

import com.multimart.dto.auth.AuthResponse;
import com.multimart.dto.auth.LoginRequest;
import com.multimart.dto.auth.RegisterRequest;
import com.multimart.dto.common.AddressDto;
import com.multimart.dto.user.UserDto;
import com.multimart.dto.user.UserResponseDto;
import com.multimart.dto.vendor.VendorDto;
import com.multimart.exception.ResourceNotFoundException;
import com.multimart.model.Address;
import com.multimart.model.Role;
import com.multimart.model.User;
import com.multimart.model.Vendor;
import com.multimart.repository.UserRepository;
import com.multimart.repository.VendorRepository;
import com.multimart.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        AddressDto addressDto = request.getAddress();
        Address address = Address.builder()
                .street(addressDto.getStreet())
                .city(addressDto.getCity())
                .state(addressDto.getState())
                .country(addressDto.getCountry())
                .zipCode(addressDto.getZipCode())
                .isDefault(addressDto.isDefault())
                .build();

        User user = User.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .address(address)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

 Vendor vendor = null;
        if (request.getRole() == Role.VENDOR && request.getStoreName() != null && request.getStoreAddress() != null) {
            AddressDto dto = request.getAddress();
            address = Address.builder()
                    .street(dto.getStreet())
                    .city(dto.getCity())
                    .state(dto.getState())
                    .country(dto.getCountry())
                    .zipCode(dto.getZipCode())
                    .isDefault(dto.isDefault())
                    .build();

            user = User.builder()
                    .username(request.getUsername())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .phoneNumber(request.getPhoneNumber())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.VENDOR)
                    .address(address)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            // ✅ Save user FIRST
            userRepository.save(user);

            AddressDto storeDto = request.getStoreAddress();
            Address storeAddress = Address.builder()
                    .street(storeDto.getStreet())
                    .city(storeDto.getCity())
                    .state(storeDto.getState())
                    .country(storeDto.getCountry())
                    .zipCode(storeDto.getZipCode())
                    .isDefault(true)
                    .build();

            vendor = Vendor.builder()
                    .storeName(request.getStoreName())
                    .storeDescription(request.getStoreDescription())
                    .specialty(request.getSpecialty())
                    .logo(request.getLogo())
                    .storeAddress(storeAddress)
                    .approvalStatus(Vendor.ApprovalStatus.PENDING)
                    .joinedDate(request.getJoinedDate())
                    .productCount(0)
                    .user(user) // ✅ now it has a saved ID
                    .build();

            vendorRepository.save(vendor);
        }

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .user(UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .phoneNumber(user.getPhoneNumber())
                        .role(user.getRole())
                        .address(AddressDto.fromEntity(user.getAddress()))
                        .vendor(vendor != null ? VendorDto.builder()
                                .storeName(vendor.getStoreName())
                                .storeDescription(vendor.getStoreDescription())
                                .specialty(vendor.getSpecialty())
                                .logo(vendor.getLogo())
                                .approvalStatus(vendor.getApprovalStatus())
                                .storeAddress(AddressDto.fromEntity(vendor.getStoreAddress()))
                                .build() : null)
                        .build())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername() != null ? request.getUsername() : request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsernameOrEmail(
                        request.getUsername(), request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(user);
        Vendor vendor = vendorRepository.findByUserId(user.getId()).orElse(null);

        return AuthResponse.builder()
                .token(token)
                .user(UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .phoneNumber(user.getPhoneNumber())
                        .role(user.getRole())
                        .address(AddressDto.fromEntity(user.getAddress()))
                        .vendor(vendor != null ? VendorDto.builder()
                                .storeName(vendor.getStoreName())
                                .storeDescription(vendor.getStoreDescription())
                                .specialty(vendor.getSpecialty())
                                .logo(vendor.getLogo())
                                .approvalStatus(vendor.getApprovalStatus())
                                .storeAddress(AddressDto.fromEntity(vendor.getStoreAddress()))
                                .build() : null)
                        .build())
                .build();
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        String token = jwtService.generateToken(user);
        emailService.sendPasswordResetEmail(email, token);
    }

    public void resetPassword(String token, String newPassword) {
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void verifyEmail(String token) {
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
    }
}
