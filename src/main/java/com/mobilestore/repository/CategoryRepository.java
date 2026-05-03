package com.mobilestore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints; // Naya import

import com.mobilestore.entity.Category;

import jakarta.persistence.QueryHint;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // 1. Website ke har page (Navbar) pe list dikhane ke liye
	@QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
	List<Category> findAll();

    // 2. Naam se category dhundhne ke liye strict method (e.g., jab koi /category/Smartphone par click kare)
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    Category findByNameIgnoreCase(String name);
}