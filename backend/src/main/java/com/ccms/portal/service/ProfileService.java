package com.ccms.portal.service;

import com.ccms.portal.dto.request.UpdateProfileRequest;
import com.ccms.portal.dto.response.UserResponse;
import com.ccms.portal.entity.UserEntity;
import com.ccms.portal.entity.UserProfileEntity;
import com.ccms.portal.exception.UserNotFoundException;
import com.ccms.portal.repository.UserProfileRepository;
import com.ccms.portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public UserResponse getUserProfile(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        UserProfileEntity profile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new UserNotFoundException("Profile not found for user ID: " + userId));

        return buildUserResponse(user, profile);
    }

    public UserResponse updateUserProfile(Long userId, UpdateProfileRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        UserProfileEntity profile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new UserNotFoundException("Profile not found for user ID: " + userId));

        boolean isEligible = request.getAnnualIncome() != null && request.getAnnualIncome() > 100000;

        // Update UserEntity fields
        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        profile.setEligibleBnpl(isEligible);

        // Update UserProfileEntity fields
        if (request.getAddress() != null) profile.setAddress(request.getAddress());
        if (request.getAnnualIncome() != null) profile.setAnnualIncome(request.getAnnualIncome());

        userRepository.save(user);
        userProfileRepository.save(profile);

        return buildUserResponse(user, profile);
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
}
