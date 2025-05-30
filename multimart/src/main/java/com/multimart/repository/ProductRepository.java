package com.multimart.repository;

import com.multimart.model.Category;
import com.multimart.model.Product;
import com.multimart.model.Subcategory;
import com.multimart.model.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategory(Category category, Pageable pageable);
    Page<Product> findBySubcategory(Subcategory subcategory, Pageable pageable);
    Page<Product> findByVendor(Vendor vendor, Pageable pageable);
    Page<Product> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);
    Page<Product> findByInStock(boolean inStock, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:vendorId IS NULL OR p.vendor.id = :vendorId) AND " +
            "(:inStock IS NULL OR p.inStock = :inStock) AND " +
            "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<Product> findWithFilters(
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("categoryId") Long categoryId,
            @Param("vendorId") Long vendorId,
            @Param("inStock") Boolean inStock,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT p FROM Product p ORDER BY p.reviewCount DESC")
    List<Product> findTrendingProducts(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p")
    int countAll();

    @Query("SELECT COUNT(p) FROM Product p WHERE MONTH(p.createdAt) = :month AND YEAR(p.createdAt) = :year")
    int countByCreatedAtMonth(@Param("month") int month, @Param("year") int year);
}
