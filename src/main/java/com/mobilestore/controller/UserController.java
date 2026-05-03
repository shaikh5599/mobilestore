package com.mobilestore.controller;

import com.mobilestore.entity.User;
import com.mobilestore.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Naya Import
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder; // SecurityConfig se aayega

    // --- REGISTRATION ---
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, HttpSession session) {
        Optional<User> existingUser = userRepo.findByEmail(user.getEmail());

        if (existingUser.isPresent()) {
            session.setAttribute("errorMsg", "Email already exists!");
            return "redirect:/register";
        } else {
            // 1. Password ko BCrypt se encode karein (Client Requirement)
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole("ROLE_USER");
            
            userRepo.save(user);
            session.setAttribute("succMsg", "Registered Successfully!");
            return "redirect:/login";
        }
    }

    // --- LOGIN PAGE ---
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    /* NOTE: processLogin() method yahan se HATA diya gaya hai.
       Ab Spring Security khud /login handle karegi background mein.
    */

    // --- LOGOUT ---
    @GetMapping("/logout")
    public String logout() {
        // Logout SecurityConfig handle karega, hum bas redirect karenge
        return "redirect:/login?logout";
    }

    @ModelAttribute
    public void commonUser(Model model, HttpSession session) {
        if (session.getAttribute("succMsg") != null) {
            model.addAttribute("succMsg", session.getAttribute("succMsg"));
            session.removeAttribute("succMsg");
        }
        if (session.getAttribute("errorMsg") != null) {
            model.addAttribute("errorMsg", session.getAttribute("errorMsg"));
            session.removeAttribute("errorMsg");
        }
    }
}