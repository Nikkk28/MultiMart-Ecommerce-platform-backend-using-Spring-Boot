package com.multimart.service;

import com.multimart.dto.common.AddressDto;
import com.multimart.dto.user.ChangePasswordRequest;
import com.multimart.dto.user.UserDto;
import com.multimart.exception.ResourceNotFoundException;
import com.multimart.model.User;
import com.multimart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToUserDto(user);
    }

    @Transactional
    public UserDto updateUserProfile(UserDto userDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update user details
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPhoneNumber(userDto.getPhoneNumber());

        // Update address if provided
        if (userDto.getAddress() != null) {
            user.setAddress(mapToAddress(userDto.getAddress()));
        }

        User updatedUser = userRepository.save(user);

        return mapToUserDto(updatedUser);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .address(user.getAddress() != null ? mapToAddressDto(user.getAddress()) : null)
                .build();
    }

    private AddressDto mapToAddressDto(com.multimart.model.Address address) {
        if (address == null) {
            return null;
        }

        return AddressDto.builder()
                .country(address.getCountry())
                .state(address.getState())
                .city(address.getCity())
                .zipCode(address.getZipCode())
                .street(address.getStreet())
                .isDefault(address.isDefault())
                .build();
    }

    private com.multimart.model.Address mapToAddress(AddressDto addressDto) {
        if (addressDto == null) {
            return null;
        }

        return com.multimart.model.Address.builder()
                .country(addressDto.getCountry())
                .state(addressDto.getState())
                .city(addressDto.getCity())
                .zipCode(addressDto.getZipCode())
                .street(addressDto.getStreet())
                .isDefault(addressDto.isDefault())
                .build();
    }
}
