package com.ccms.portal.controller;

import com.ccms.portal.dto.request.LoginRequest;
import com.ccms.portal.dto.request.RegisterRequest;
import com.ccms.portal.dto.response.AuthResponse;
import com.ccms.portal.dto.response.UserResponse;
import com.ccms.portal.service.AuthService;
import com.ccms.portal.util.TokenblackList;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private TokenblackList tokenblackList;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("anchal@example.com");

        UserResponse expectedResponse = UserResponse.builder()
                .email("anchal@example.com")
                .name("Anchal")
                .build();

        when(authService.register(request)).thenReturn(expectedResponse);

        UserResponse actualResponse = authController.register(request);

        assertNotNull(actualResponse);
        assertEquals("anchal@example.com", actualResponse.getEmail());
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest("anchal@example.com", "securePass");

        AuthResponse authResponse = new AuthResponse("access-token", "refresh-token",
                UserResponse.builder().email("anchal@example.com").build());

        when(authService.login(eq(request))).thenReturn(authResponse);

        AuthResponse response = authController.login(request, httpServletResponse);

        assertNotNull(response);
        assertEquals("access-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(httpServletResponse).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
    }

    @Test
    void logout_withValidToken() {
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer valid-token");

        String result = authController.logout(httpServletRequest);

        assertEquals("Successfully logged out.", result);
        verify(tokenblackList).blacklist("valid-token");
    }

    @Test
    void logout_withoutToken() {
        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

        String result = authController.logout(httpServletRequest);

        assertEquals("No token provided.", result);
        verify(tokenblackList, never()).blacklist(anyString());
    }

    @Test
    void refreshToken_success() {
        String refreshToken = "valid-refresh-token";

        AuthResponse authResponse = new AuthResponse("new-access-token", "new-refresh-token",
                UserResponse.builder().email("anchal@example.com").build());

        when(tokenblackList.isBlacklisted(refreshToken)).thenReturn(false);
        when(authService.refreshAccessToken(refreshToken)).thenReturn(authResponse);

        AuthResponse response = authController.refreshToken(refreshToken, httpServletResponse);

        assertNotNull(response);
        assertEquals("new-access-token", response.getToken());
        verify(httpServletResponse).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
    }

    @Test
    void refreshToken_blacklisted() {
        String refreshToken = "blacklisted-token";

        when(tokenblackList.isBlacklisted(refreshToken)).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> authController.refreshToken(refreshToken, httpServletResponse));
    }

    @Test
    void refreshToken_nullToken() {
        assertThrows(RuntimeException.class,
                () -> authController.refreshToken(null, httpServletResponse));
    }
}
