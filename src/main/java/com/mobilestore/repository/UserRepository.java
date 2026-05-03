package com.mobilestore.repository;



import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilestore.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Login ke waqt hum email se user ko dhoondhenge
	// Professional Way
	Optional<User> findByEmail(String email);
	List<User> findByRole(String role);
}