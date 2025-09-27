package com.ccms.portal.controller;

import com.ccms.portal.dto.response.UserResponse;
import com.ccms.portal.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProfileController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable Long userId) {
        log.info("Fetching profile for user: {}", userId);

        try {
            UserResponse profile = userService.getUserProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error fetching profile for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(404).build();
        }
    }
}