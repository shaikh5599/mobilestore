package com.mobilestore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import com.mobilestore.entity.StoreSetting;
import com.mobilestore.entity.User;
import com.mobilestore.repository.CartRepository;
import com.mobilestore.repository.StoreRepository;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice // <--- BINA ISKE KAAM NAHI HOGA!
public class GlobalControllerAdvice {

    @Autowired
    private CartRepository cartRepo;

    @ModelAttribute
    public void addCommonAttributes(Model model, @RequestParam(value = "keyword", required = false) String keyword, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        
        long cartCount = 0;
        if(user != null) {
            cartCount = cartRepo.findByUser(user).size(); 
            // YE LINE DATA HTML KO BHEJTI HAI
            model.addAttribute("loggedInUser", user); 
        }
        
        model.addAttribute("globalCartCount", cartCount);
        model.addAttribute("keyword", keyword); 
    }
    @Autowired
    private StoreRepository storeRepo;

    @ControllerAdvice
    public class GlobalController {

        @Autowired
        private StoreRepository storeRepo;

        @ModelAttribute
        public void commonData(Model model) {
            // Database se ID 1 wali row uthao
            StoreSetting store = storeRepo.findById(1L).orElse(new StoreSetting());
            
            // Agar database khali hai toh ek default naam set kar do
            if(store.getStoreName() == null) {
                store.setStoreName("My Mobile Shop");
            }
            
            // Ye 'store' object ab har HTML page (Header/Footer) mein kaam karega
            model.addAttribute("store", store);
        }
    }
}