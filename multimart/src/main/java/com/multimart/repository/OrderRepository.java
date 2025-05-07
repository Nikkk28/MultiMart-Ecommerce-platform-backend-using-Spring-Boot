package com.multimart.repository;

import com.multimart.model.Order;
import com.multimart.model.User;
import com.multimart.model.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUser(User user, Pageable pageable);
    
    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.vendor = :vendor")
    Page<Order> findByVendor(Vendor vendor, Pageable pageable);
    
    Page<Order> findByUserAndStatus(User user, Order.OrderStatus status, Pageable pageable);
    
    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.vendor = :vendor AND o.status = :status")
    Page<Order> findByVendorAndStatus(Vendor vendor, Order.OrderStatus status, Pageable pageable);
}
