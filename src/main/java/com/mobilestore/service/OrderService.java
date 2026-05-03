package com.mobilestore.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict; // <-- Naya Import
import org.springframework.stereotype.Service;

import com.mobilestore.entity.CartItem;
import com.mobilestore.entity.Order;
import com.mobilestore.entity.OrderItem;
import com.mobilestore.entity.Product;
import com.mobilestore.entity.User;
import com.mobilestore.repository.OrderRepository;
import com.mobilestore.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OrderService(ProductRepository productRepository,
                        OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    // YAHAN CACHE EVICT LAGAYA HAI
    // Jaise hi ye method success hoga, purane products ki list memory se delete ho jayegi!
    @Transactional
    @CacheEvict(value = {"products", "latest_products", "category_products"}, allEntries = true)
    public void placeCartOrder(List<CartItem> cartItems, User user, String address, String pincode) {
        
        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setPincode(pincode);
        order.setStatus("PLACED");
        order.setOrderDate(LocalDateTime.now());
        
        double totalOrderAmount = 0;
        List<OrderItem> orderItemsList = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // 1. Stock Check aur Reduce
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Product " + product.getName() + " out of stock!");
            }
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            // 2. OrderItem banayein
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(product.getPrice());
            orderItemsList.add(item);

            totalOrderAmount += (product.getPrice() * cartItem.getQuantity());
        }

        order.setOrderItems(orderItemsList);
        order.setTotalAmount(totalOrderAmount);
        
        // 3. Pura Order save karein
        orderRepository.save(order);
    }
}