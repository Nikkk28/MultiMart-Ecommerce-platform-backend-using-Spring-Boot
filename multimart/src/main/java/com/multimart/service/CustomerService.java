package com.multimart.service;

import com.multimart.dto.customer.CustomerAddressDto;
import com.multimart.dto.customer.CustomerDashboardDto;
import com.multimart.dto.order.OrderSummaryDto;
import com.multimart.exception.ResourceNotFoundException;
import com.multimart.model.Cart;
import com.multimart.model.User;
import com.multimart.model.UserAddress;
import com.multimart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final OrderService orderService;

    public CustomerDashboardDto getCustomerDashboard(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get order count
        int orderCount = orderRepository.findByUser(user, PageRequest.of(0, 1)).getTotalPages();

        // Get recent orders
        List<OrderSummaryDto> recentOrders = orderRepository.findByUser(
                        user,
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
                )
                .map(orderService::mapToOrderSummaryDto)
                .getContent();

        // Get cart info
        Cart cart = cartRepository.findByUser(user).orElse(null);
        int cartItems = cart != null ? cart.getTotalItems() : 0;
        double cartTotal = cart != null ? cart.getTotal() : 0.0;

        // Get wishlist count
        int wishlistCount = wishlistItemRepository.findByUser(user).size();

        // Get saved address count
        int savedAddressCount = userAddressRepository.findByUser(user).size();

        return CustomerDashboardDto.builder()
                .orderCount(orderCount)
                .recentOrders(recentOrders)
                .cartItems(cartItems)
                .cartTotal(cartTotal)
                .wishlistCount(wishlistCount)
                .savedAddressCount(savedAddressCount)
                .build();
    }

    public List<CustomerAddressDto> getCustomerAddresses(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userAddressRepository.findByUser(user).stream()
                .map(this::mapToCustomerAddressDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerAddressDto addCustomerAddress(CustomerAddressDto addressDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // If this is the first address or marked as default, update existing default
        if (addressDto.isDefault()) {
            userAddressRepository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(address -> {
                        address.setDefault(false);
                        userAddressRepository.save(address);
                    });
        }

        // Create new address
        UserAddress address = UserAddress.builder()
                .user(user)
                .country(addressDto.getCountry())
                .state(addressDto.getState())
                .city(addressDto.getCity())
                .zipCode(addressDto.getZipCode())
                .street(addressDto.getStreet())
                .isDefault(addressDto.isDefault())
                .label(addressDto.getLabel() != null ? addressDto.getLabel() : UserAddress.AddressLabel.HOME)
                .build();

        UserAddress savedAddress = userAddressRepository.save(address);

        return mapToCustomerAddressDto(savedAddress);
    }

    @Transactional
    public CustomerAddressDto updateCustomerAddress(Long addressId, CustomerAddressDto addressDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Ensure the address belongs to the user
        if (!address.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Address does not belong to user");
        }

        // If this address is being set as default, update existing default
        if (addressDto.isDefault() && !address.isDefault()) {
            userAddressRepository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(defaultAddress -> {
                        defaultAddress.setDefault(false);
                        userAddressRepository.save(defaultAddress);
                    });
        }

        // Update address
        address.setCountry(addressDto.getCountry());
        address.setState(addressDto.getState());
        address.setCity(addressDto.getCity());
        address.setZipCode(addressDto.getZipCode());
        address.setStreet(addressDto.getStreet());
        address.setDefault(addressDto.isDefault());
        address.setLabel(addressDto.getLabel() != null ? addressDto.getLabel() : address.getLabel());

        UserAddress updatedAddress = userAddressRepository.save(address);

        return mapToCustomerAddressDto(updatedAddress);
    }

    @Transactional
    public void deleteCustomerAddress(Long addressId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Ensure the address belongs to the user
        if (!address.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Address does not belong to user");
        }

        // If this is the default address, find another address to make default
        if (address.isDefault()) {
            List<UserAddress> otherAddresses = userAddressRepository.findByUser(user).stream()
                    .filter(a -> !a.getId().equals(addressId))
                    .collect(Collectors.toList());

            if (!otherAddresses.isEmpty()) {
                UserAddress newDefault = otherAddresses.get(0);
                newDefault.setDefault(true);
                userAddressRepository.save(newDefault);
            }
        }

        userAddressRepository.delete(address);
    }

    private CustomerAddressDto mapToCustomerAddressDto(UserAddress address) {
        return CustomerAddressDto.builder()
                .id(address.getId())
                .country(address.getCountry())
                .state(address.getState())
                .city(address.getCity())
                .zipCode(address.getZipCode())
                .street(address.getStreet())
                .isDefault(address.isDefault())
                .label(address.getLabel())
                .build();
    }
}
