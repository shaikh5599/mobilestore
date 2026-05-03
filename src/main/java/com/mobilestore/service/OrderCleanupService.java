package com.mobilestore.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mobilestore.entity.Order;
import com.mobilestore.repository.OrderItemRepository;
import com.mobilestore.repository.OrderRepository;

import jakarta.transaction.Transactional;
@Service
public class OrderCleanupService {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private OrderItemRepository orderItemRepo;

    // Har 24 ghante mein chalega (86400000 milliseconds)
    @Scheduled(fixedRate = 86400000) 
    @Transactional
    public void deleteCancelledOrders() {
        // 1. Saare Cancelled orders dhoondo
        List<Order> cancelledOrders = orderRepo.findByStatusContaining("Cancelled");

        for (Order order : cancelledOrders) {
            // 2. Pehle Order ke items delete karo (Foreign key constraint ki wajah se)
            orderItemRepo.deleteByOrderId(order.getId());
            
            // 3. Fir main Order delete karo
            orderRepo.delete(order);
        }
        
        System.out.println("Cleanup Done: Purane cancelled orders database se hata diye gaye hain.");
    }
}