package com.mobilestore.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilestore.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    // Ye method humein tab kaam aayega jab hum login ya security pe jayenge
    Admin findByEmail(String email);
}