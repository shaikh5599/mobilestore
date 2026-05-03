package com.mobilestore.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mobilestore.entity.CartItem;
import com.mobilestore.entity.Order;
import com.mobilestore.entity.OrderItem;
import com.mobilestore.entity.Product;
import com.mobilestore.entity.User;
import com.mobilestore.repository.CartRepository;
import com.mobilestore.repository.OrderRepository;
import com.mobilestore.repository.ProductRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class OrderController {

    @Autowired private OrderRepository orderRepo;
    @Autowired private CartRepository cartRepo;
    @Autowired private ProductRepository productRepos;

    @PostMapping("/order/buy-now/{productId}")
    public String buyNow(@PathVariable Long productId, 
                         @RequestParam("quantity") int quantity, 
                         @RequestParam(value = "selectedVariant", required = false) String selectedVariant,
                         HttpSession session) {
        
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        Product p = productRepos.findById(productId).orElse(null);
        
        if (p == null || p.getStock() < quantity) {
            session.setAttribute("errorMsg", "OUT OF STOCK! Admin ke stock add karne tak intezar karein.");
            return "redirect:/product/detail/" + productId;
        }

        CartItem cartItem = new CartItem();
        cartItem.setProduct(p);
        cartItem.setUser(user);
        cartItem.setQuantity(quantity);
        cartItem.setTotalPrice(p.getPrice() * quantity);
        cartItem.setSelectedVariant(selectedVariant);
        cartRepo.save(cartItem);

        return "redirect:/checkout"; 
    }

    @GetMapping("/checkout")
    public String showCheckout(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        List<CartItem> cartItems = cartRepo.findByUser(user);
        if (cartItems.isEmpty()) return "redirect:/cart"; 

        double grandTotal = cartItems.stream().mapToDouble(CartItem::getTotalPrice).sum();
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("grandTotal", grandTotal);
        
        return "checkout";
    }

    @PostMapping("/place-order")
    @Transactional
    public String placeOrder(
            @RequestParam String fullName, 
            @RequestParam String email, 
            @RequestParam String mobileNumber, 
            @RequestParam String address,
            @RequestParam String city,      
            @RequestParam String pincode,   
            HttpSession session) {
        
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        List<CartItem> cartItems = cartRepo.findByUser(user);
        if (cartItems.isEmpty()) return "redirect:/cart";

        for (CartItem item : cartItems) {
            int result = productRepos.reduceStock(item.getProduct().getId(), item.getQuantity());
            if (result == 0) {
                session.setAttribute("errorMsg", "Sorry! " + item.getProduct().getName() + " out of stock ho gaya hai.");
                return "redirect:/cart"; 
            }
        }

        double total = cartItems.stream().mapToDouble(CartItem::getTotalPrice).sum();

        Order order = new Order();
        order.setFullName(fullName);
        order.setEmail(email);
        order.setMobileNumber(mobileNumber);
        order.setAddress(address);
        order.setCity(city);        
        order.setPincode(pincode);  
        order.setTotalAmount(total);
        order.setStatus("SUCCESS"); 
        order.setOrderDate(LocalDateTime.now());
        order.setUser(user);        

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setProduct(cartItem.getProduct());
            oi.setQuantity(cartItem.getQuantity());
            oi.setPrice(cartItem.getProduct().getPrice());
            oi.setSelectedVariant(cartItem.getSelectedVariant()); // Variant Order mein copy hua
            orderItems.add(oi);
        }
        order.setOrderItems(orderItems);

        try {
            orderRepo.save(order);
            cartRepo.deleteAll(cartItems);
            session.setAttribute("succMsg", "Congratulations! Order successfully place ho gaya.");
            return "redirect:/order-success?id=" + order.getId();
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Order Failed due to technical error.");
            return "redirect:/cart";
        }
    }

    @GetMapping("/order-success")
    public String orderSuccess(@RequestParam Long id, Model model) {
        model.addAttribute("orderId", id);
        return "order-success"; 
    }
    
    @GetMapping("/my-orders")
    public String myOrders(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        List<Order> orders = orderRepo.findByUserOrderByOrderDateDesc(user);
        model.addAttribute("orders", orders);
        return "my-orders";
    }
    
    @GetMapping("/order-details/{id}")
    public String viewUserOrderDetails(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        Order order = orderRepo.findById(id).orElse(null);
        
        if (order == null || !order.getUser().getId().equals(user.getId())) {
            return "redirect:/my-orders";
        }

        model.addAttribute("order", order);
        return "user-order-details"; 
    }
    
    @PostMapping("/cancel-order/{id}")
    @Transactional
    public String cancelOrder(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        Order order = orderRepo.findById(id).orElse(null);

        if (order != null && order.getUser().getId().equals(user.getId())) {
            if (order.getStatus().equalsIgnoreCase("SUCCESS") || order.getStatus().equalsIgnoreCase("PENDING")) {
                
                for (OrderItem item : order.getOrderItems()) {
                    Product product = item.getProduct();
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepos.save(product);
                }

                order.setStatus("CANCELLED");
                orderRepo.save(order);

                ra.addFlashAttribute("succMsg", "Order Cancelled! Stock is restored.");
            } else {
                ra.addFlashAttribute("errorMsg", "Order cannot be cancelled. Status: " + order.getStatus());
            }
        }

        return "redirect:/my-orders";
    }
}