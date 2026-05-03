package com.mobilestore.repository;

import com.mobilestore.entity.CartItem;
import com.mobilestore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Long> {
    
    // Sirf login user ka poora cart nikalne ke liye
    List<CartItem> findByUser(User user);
    
    // Check karne ke liye ki us user ke cart mein ye product pehle se hai ya nahi
    CartItem findByProductIdAndUser(Long productId, User user);
    
}