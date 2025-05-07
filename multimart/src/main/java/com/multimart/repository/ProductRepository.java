package com.multimart.repository;

import com.multimart.model.Category;
import com.multimart.model.Product;
import com.multimart.model.Subcategory;
import com.multimart.model.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> findWithFilters(
            Double minPrice, 
            Double maxPrice, 
            Long categoryId, 
            Long vendorId, 
            Boolean inStock, 
            String search, 
            Pageable pageable);
    
    @Query("SELECT p FROM Product p ORDER BY p.reviewCount DESC")
    List<Product> findTrendingProducts(Pageable pageable);
}
