package com.multimart.repository;

import com.multimart.model.User;
import com.multimart.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUser(User user);
    Optional<UserAddress> findByUserAndIsDefaultTrue(User user);
}
