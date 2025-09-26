package com.ccms.portal.service;

import com.ccms.portal.dto.request.LoginRequest;
import com.ccms.portal.dto.request.RegisterRequest;
import com.ccms.portal.dto.response.AuthResponse;
import com.ccms.portal.dto.response.UserResponse;
import com.ccms.portal.entity.UserEntity;
import com.ccms.portal.enums.UserRole;
import com.ccms.portal.entity.UserProfileEntity;
import com.ccms.portal.exception.UnauthorizedApplicationActionException;
import com.ccms.portal.repository.UserProfileRepository;
import com.ccms.portal.repository.UserRepository;
import com.ccms.portal.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        UserEntity user = UserEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.USER)
                .build();

        userRepository.save(user);

        boolean isEligible = request.getAnnualIncome() != null && request.getAnnualIncome() > 100000;

        UserProfileEntity profile = UserProfileEntity.builder()
                .user(user)
                .address(request.getAddress())
                .annualIncome(request.getAnnualIncome())
                .isEligibleBnpl(isEligible)
                .build();

        userProfileRepository.save(profile);

        return buildUserResponse(user, profile);
    }

    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        UserProfileEntity profile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        String token = jwtUtil.generateToken(user.getEmail(),user.getRole().toString(),user.getId());
        return new AuthResponse(token, buildUserResponse(user, profile));
    }

    private UserResponse buildUserResponse(UserEntity user, UserProfileEntity profile) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(profile.getAddress())
                .isEligibleForBNPL(profile.isEligibleBnpl())
                .build();
    }

    public void verifyPasswordForEmail(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            throw new UnauthorizedApplicationActionException("Authentication required");
        }
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedApplicationActionException("User not found"));
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new UnauthorizedApplicationActionException("Invalid password");
        }
    }
}
