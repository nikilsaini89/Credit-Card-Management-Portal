package com.ccms.portal.service;

import com.ccms.portal.dto.response.UserResponse;
import com.ccms.portal.entity.UserEntity;
import com.ccms.portal.entity.UserProfileEntity;
import com.ccms.portal.exception.ResourceNotFoundException;
import com.ccms.portal.repository.UserRepository;
import com.ccms.portal.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;
  private final UserProfileRepository userProfileRepository;

  public UserResponse getUserProfile(Long userId) {
    log.info("Fetching user profile for user: {}", userId);

    UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    UserProfileEntity profile = userProfileRepository.findByUser(user)
        .orElseThrow(() -> new ResourceNotFoundException("User profile not found for user ID: " + userId));

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
