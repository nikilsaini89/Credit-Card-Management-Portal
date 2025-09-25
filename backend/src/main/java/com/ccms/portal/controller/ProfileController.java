package com.ccms.portal.controller;

import com.ccms.portal.dto.request.UpdateProfileRequest;
import com.ccms.portal.dto.response.UserResponse;
import com.ccms.portal.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/{userId}")
    public UserResponse getProfile(@PathVariable Long userId) {
        return profileService.getUserProfile(userId);
    }

    @PutMapping("/{userId}")
    public UserResponse updateProfile(@PathVariable Long userId,
                                      @RequestBody UpdateProfileRequest request) {
        return profileService.updateUserProfile(userId, request);
    }
}
