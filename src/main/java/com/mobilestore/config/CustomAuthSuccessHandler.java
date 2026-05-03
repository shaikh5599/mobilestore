package com.mobilestore.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.mobilestore.entity.User;
import com.mobilestore.repository.UserRepository;

import java.io.IOException;
import java.util.Set;

@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    // 1. UserRepository ko yahan laye taaki user ki details nikal sakein
    @Autowired
    private UserRepository userRepo;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        // 2. Login karne wale ka email nikalna
        String email = authentication.getName();
        
        // 3. Database se us user ka poora data nikalna
        User user = userRepo.findByEmail(email).orElse(null);
        
        // 4. US DATA KO SESSION MEIN DAALNA (Yahi step missing tha!)
        if (user != null) {
            request.getSession().setAttribute("loggedInUser", user);
        }

        // 5. Purana wala Redirect Logic
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        if (roles.contains("ROLE_ADMIN")) {
            response.sendRedirect("/admin/dashboard");
        } else {
            response.sendRedirect("/");
        }
    }
}