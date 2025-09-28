package com.ccms.portal.controller;


import com.ccms.portal.dto.request.LoginRequest;
import com.ccms.portal.dto.request.RegisterRequest;
import com.ccms.portal.dto.response.AuthResponse;
import com.ccms.portal.dto.response.UserResponse;
import com.ccms.portal.service.AuthService;
import com.ccms.portal.util.TokenblackList;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
    public AuthResponse login(@RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return authResponse;
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

    @PostMapping("/refresh")
    public AuthResponse refreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || tokenBlacklist.isBlacklisted(refreshToken)) {
            throw new RuntimeException("Invalid or blacklisted refresh token");
        }

        return authService.refreshAccessToken(refreshToken);
    }


    @GetMapping("/home")
    public String home() {

        return "Welcome to home page, ";
    }
}
