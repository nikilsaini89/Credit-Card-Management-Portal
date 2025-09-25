package com.ccms.portal.controller;


import com.ccms.portal.dto.request.LoginRequest;
import com.ccms.portal.dto.request.RegisterRequest;
import com.ccms.portal.dto.response.AuthResponse;
import com.ccms.portal.dto.response.UserResponse;
import com.ccms.portal.service.AuthService;
import com.ccms.portal.util.TokenblackList;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenblackList tokenBlacklist;

    @PostMapping("/register")
    public UserResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklist.blacklist(token);
            return "Successfully logged out.";
        }
        return "No token provided.";
    }

    @GetMapping("/home")
    public String home() {

        return "Welcome to home page, ";
    }
}
