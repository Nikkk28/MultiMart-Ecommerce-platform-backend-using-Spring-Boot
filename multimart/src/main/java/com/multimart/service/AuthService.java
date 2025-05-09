package com.multimart.service;

import com.multimart.dto.auth.AuthResponse;
import com.multimart.dto.auth.LoginRequest;
import com.multimart.dto.auth.RegisterRequest;
import com.multimart.dto.user.UserDto;
import com.multimart.exception.ResourceNotFoundException;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final VendorRepository vendorRepository;

    public void register(RegisterRequest request) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Create new user
        if(request.getRole()==Role.CUSTOMER) {
            User user = User.builder()
                    .username(request.getUsername())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .phoneNumber(request.getPhoneNumber())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.CUSTOMER)
                    .enabled(true)
                    .address(request.getAddress())
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            userRepository.save(user);


            // Send verification email
            String token = jwtService.generateToken(user);
//        emailService.sendVerificationEmail(user.getEmail(), token);
        }
        if (request.getRole()==Role.VENDOR) {
            User user = User.builder()
                    .username(request.getUsername())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .phoneNumber(request.getPhoneNumber())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.VENDOR)
                    .address(request.getAddress())
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();
            Vendor vendor= Vendor.builder()
                    .storeName(request.getStoreName())
                    .storeAddress(request.getStoreAddress())
                    .storeDescription(request.getStoreDescription())
                    .logo(request.getLogo())
                    .specialty(request.getSpecialty())
                    .rejectionReason(request.getRejectionReason())
                    .rating(request.getRating())
                    .productCount(request.getProductCount())
                    .joinedDate(request.getJoinedDate())
                    .user(user)
                    .approvalStatus(Vendor.ApprovalStatus.PENDING)
                    .build();

            userRepository.save(user);
            vendorRepository.save(vendor);
        }
        if (request.getRole()==Role.ADMIN) {
            User user = User.builder()
                    .username(request.getUsername())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .phoneNumber(request.getPhoneNumber())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.ADMIN)
                    .address(request.getAddress())
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            userRepository.save(user);
        }
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername() != null ? request.getUsername() : request.getEmail(),
                        request.getPassword()
                )
        );
        
        // Find user
        User user = userRepository.findByUsernameOrEmail(
                request.getUsername(), request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Generate token
        String token = jwtService.generateToken(user);
        
        // Map user to DTO
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .build();
        
        return AuthResponse.builder()
                .token(token)
                .user(userDto)
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
