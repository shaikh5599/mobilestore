package com.mobilestore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mobilestore.entity.Order;
import com.mobilestore.entity.User;

import jakarta.transaction.Transactional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
	// Saare orders ki total price ka sum nikalne ke liye
    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    Double getTotalRevenue();

    // Agar aap sirf 'Success' orders ka total chahte hain (Better Approach)
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'Delivered'")
    Double getTotalDeliveredRevenue();
	
    // User ki order history nikalne ke liye
    List<Order> findByUserOrderByOrderDateDesc(User user);
 // Yeh custom query product ko child table se udane ke liye hai
    @Transactional
    @Modifying
    @Query("DELETE FROM OrderItem o WHERE o.product.id = :productId")
    void deleteByProductId(Long productId);
    List<Order> findByStatusContaining(String status);
}