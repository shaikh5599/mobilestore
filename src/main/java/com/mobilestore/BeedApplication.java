package com.mobilestore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mobilestore.entity.User;
import com.mobilestore.repository.UserRepository;

@SpringBootApplication
public class BeedApplication implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(BeedApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        
        // Database me check karega ki admin email pehle se hai ya nahi
        if (userRepository.findByEmail("admin@gmail.com") == null) {
            
            User admin = new User(); 
            admin.setName("Admin");
            admin.setEmail("shaikhmudasir@gmail.com");
            
            // Register form ki baaki zaroori fields yahan set ki hain
            admin.setMobileNumber("0000000000"); 
            admin.setCity("Mumbai");
            admin.setState("Maharashtra");
            
            // Spring Security ke hisab se password encrypt kar rahe hain
            admin.setPassword(passwordEncoder.encode("Mudasir@gmail123")); 
            admin.setRole("ROLE_ADMIN"); 
            
            userRepository.save(admin);
            
            System.out.println("==========================================");
            System.out.println("✅ Live Database me Admin Account Ban Gaya!");
            System.out.println("Email: admin@gmail.com | Password: admin123");
            System.out.println("==========================================");
        }
    }
}