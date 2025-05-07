package com.multimart.repository;

import com.multimart.model.Product;
import com.multimart.model.Review;
import com.multimart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);
    Optional<Review> findByUserAndProduct(User user, Product product);
}
