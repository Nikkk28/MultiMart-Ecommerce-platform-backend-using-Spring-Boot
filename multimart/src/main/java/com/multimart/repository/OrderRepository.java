package com.multimart.repository;

import com.multimart.model.Order;
import com.multimart.model.User;
import com.multimart.model.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUser(User user, Pageable pageable);

    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.vendor = :vendor")
    Page<Order> findByVendor(Vendor vendor, Pageable pageable);

    Page<Order> findByUserAndStatus(User user, Order.OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.vendor = :vendor AND o.status = :status")
    Page<Order> findByVendorAndStatus(Vendor vendor, Order.OrderStatus status, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate")
    int countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query(value = """
        SELECT COALESCE(SUM(o.total), 0)
        FROM orders o
        WHERE o.vendor_id = :vendorId
          AND o.status = 'DELIVERED'
        """, nativeQuery = true)
    Double getTotalRevenue(@Param("vendorId") Long vendorId);


    @Query(value = """
        SELECT COALESCE(SUM(o.total), 0)
        FROM orders o
        WHERE o.vendor_id = :vendorId
          AND o.status = 'DELIVERED'
          AND EXTRACT(YEAR FROM o.created_at) = :year
          AND EXTRACT(MONTH FROM o.created_at) = :month
        """, nativeQuery = true)
    Double getMonthlyRevenue(@Param("vendorId") Long vendorId,
                             @Param("year") int year,
                             @Param("month") int month);
    @Query("SELECT COUNT(o) FROM Order o")
    int countAll();

    @Query("SELECT COUNT(o) FROM Order o WHERE MONTH(o.createdAt) = :month AND YEAR(o.createdAt) = :year")
    int countByCreatedAtMonth(@Param("month") int month, @Param("year") int year);

    @Query("SELECT SUM(o.total) FROM Order o WHERE o.status = 'DELIVERED'")
    Double getTotalRevenue();

    @Query("SELECT SUM(o.total) FROM Order o WHERE o.status = 'DELIVERED' AND MONTH(o.createdAt) = :month AND YEAR(o.createdAt) = :year")
    Double getMonthlyRevenue(@Param("month") int month, @Param("year") int year);

}
