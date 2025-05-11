package com.multimart.repository;

import com.multimart.model.Role;
import com.multimart.model.User;
import com.multimart.model.UserAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    @Query("SELECT COUNT(u) FROM User u")
    int countAll();
    Page<User> findByRole(Role role, Pageable pageable);

    @Query(value = """
    SELECT COUNT(*) FROM users 
    WHERE EXTRACT(MONTH FROM created_at) = :month 
      AND EXTRACT(YEAR FROM created_at) = :year
""", nativeQuery = true)
    int countByCreatedAtMonth(@Param("month") int month, @Param("year") int year);


    int countByRole(Role role);

}
