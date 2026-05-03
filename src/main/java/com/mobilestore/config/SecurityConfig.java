package com.mobilestore.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private CustomAuthSuccessHandler successHandler;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); 
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .authorizeHttpRequests(auth -> auth
            	    // FIX YAHAN HAI: hasRole ki jagah hasAuthority use kiya hai
            	    .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN") 
            	    .requestMatchers("/user/**").hasAuthority("ROLE_USER")               
            	    .requestMatchers("/**").permitAll() 
            	
            )
            // YAHAN FIX KIYA HAI: Sirf ek formLogin block rakha hai
            .formLogin(login -> login
                .loginPage("/login") 
                .loginProcessingUrl("/login") 
                .successHandler(successHandler) 
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}