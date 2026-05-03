package com.mobilestore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mobilestore.entity.CartItem;
import com.mobilestore.entity.Product;
import com.mobilestore.entity.User;
import com.mobilestore.repository.CartRepository;
import com.mobilestore.repository.ProductRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

    @Autowired private CartRepository cartRepo;
    @Autowired private ProductRepository productRepo;

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login"; 
        }

        List<CartItem> cartItems = cartRepo.findByUser(user);
        
        double grandTotal = cartItems.stream().mapToDouble(CartItem::getTotalPrice).sum();
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("grandTotal", grandTotal);
        
        return "cart"; 
    }

    @PostMapping("/cart/add/{id}")
    public String addToCart(@PathVariable Long id, 
                            @RequestParam("quantity") int quantity, 
                            @RequestParam(value = "selectedVariant", required = false) String selectedVariant,
                            HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        List<CartItem> userItems = cartRepo.findByUser(user);
        CartItem existingItem = null;
        
        // Variant aur Product dono match hone chahiye
        for(CartItem item : userItems) {
            if(item.getProduct().getId().equals(id)) {
                if((selectedVariant == null && item.getSelectedVariant() == null) || 
                   (selectedVariant != null && selectedVariant.equals(item.getSelectedVariant()))) {
                    existingItem = item;
                    break;
                }
            }
        }
        
        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            existingItem.setQuantity(newQuantity);
            existingItem.setTotalPrice(existingItem.getProduct().getPrice() * newQuantity);
            cartRepo.save(existingItem);
        } else {
            Product product = productRepo.findById(id).orElse(null);
            if (product != null) {
                CartItem newItem = new CartItem();
                newItem.setProduct(product);
                newItem.setQuantity(quantity);
                newItem.setTotalPrice(product.getPrice() * quantity);
                newItem.setUser(user);
                newItem.setSelectedVariant(selectedVariant);
                cartRepo.save(newItem);
            }
        }
        return "redirect:/cart";
    }

    @GetMapping("/cart/delete/{id}")
    public String deleteCartItem(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";
        
        cartRepo.deleteById(id);
        return "redirect:/cart";
    }
 
    @PostMapping("/buy-now/{id}")
    public String buyNow(@PathVariable Long id, 
                         @RequestParam("quantity") int quantity, 
                         @RequestParam(value = "selectedVariant", required = false) String selectedVariant,
                         HttpSession session) {
        
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        List<CartItem> userItems = cartRepo.findByUser(user);
        CartItem existingItem = null;
        
        for(CartItem item : userItems) {
            if(item.getProduct().getId().equals(id)) {
                if((selectedVariant == null && item.getSelectedVariant() == null) || 
                   (selectedVariant != null && selectedVariant.equals(item.getSelectedVariant()))) {
                    existingItem = item;
                    break;
                }
            }
        }
        
        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            existingItem.setQuantity(newQuantity);
            existingItem.setTotalPrice(existingItem.getProduct().getPrice() * newQuantity);
            cartRepo.save(existingItem);
        } else {
            Product product = productRepo.findById(id).orElse(null);
            if (product != null) {
                CartItem newItem = new CartItem();
                newItem.setProduct(product);
                newItem.setQuantity(quantity);
                newItem.setTotalPrice(product.getPrice() * quantity);
                newItem.setUser(user);
                newItem.setSelectedVariant(selectedVariant);
                cartRepo.save(newItem);
            }
        }
        return "redirect:/checkout"; 
    }
}