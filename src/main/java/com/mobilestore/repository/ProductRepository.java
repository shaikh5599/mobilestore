package com.mobilestore.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints; // Yeh import add karna
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobilestore.entity.Product;

import jakarta.persistence.QueryHint;
import jakarta.transaction.Transactional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 🔴 NO CACHE: Search query
    List<Product> findByNameContainingIgnoreCase(String name);
	
    // 🔴 NO CACHE: Search query
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(concat('%', :keyword, '%')) " +
           "OR LOWER(p.brand) LIKE LOWER(concat('%', :keyword, '%'))")
    List<Product> searchProducts(@Param("keyword") String keyword);

    // 🟢 CACHE ENABLED: Brand filter
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    List<Product> findByBrandIgnoreCase(String brand);

    // 🟢 CACHE ENABLED: Home page latest products
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    List<Product> findTop8ByOrderByIdDesc();

    // 🟢 CACHE ENABLED: Category products
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    List<Product> findByCategoryId(Long categoryId);
    
    // ⚠️ NO CACHE: Inventory modifying query
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.stock = p.stock - :qty WHERE p.id = :id AND p.stock >= :qty")
    int reduceStock(@Param("id") Long id, @Param("qty") int qty);

}