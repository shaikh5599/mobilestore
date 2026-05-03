package com.mobilestore.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mobilestore.entity.User;
import com.mobilestore.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        // YEH LINE ADD KIJIYE DEBUGGING KE LIYE
        System.out.println("🔍 DEBUG: Attempting to login with email: " + email);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("❌ ERROR: Email not found in Database!");
                    return new UsernameNotFoundException("User not found");
                });

        System.out.println("✅ SUCCESS: User found! Role is: " + user.getRole());

        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().replace("ROLE_", ""))
                .build();
    }}