package com.multimart.repository;

import com.multimart.model.User;
import com.multimart.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByUser(User user);
    boolean existsByStoreName(String storeName);
}
