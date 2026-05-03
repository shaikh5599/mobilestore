package com.mobilestore.repository;

import org.springframework.data.jpa.repository.JpaRepository; // Ye import add karein
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mobilestore.entity.OrderItem; // Apni OrderItem entity ko import karein

import jakarta.transaction.Transactional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> { // <--- YEH LINE ZAROORI HAI
	
	@Transactional
	@Modifying
	@Query("DELETE FROM OrderItem o WHERE o.order.id = :orderId")
	void deleteByOrderId(Long orderId);

    // Agar delete product wala kaam bhi isi se karwana hai toh ye bhi add kar lo:
    @Transactional
    @Modifying
    @Query("DELETE FROM OrderItem o WHERE o.product.id = :productId")
    void deleteByProductId(Long productId);
}