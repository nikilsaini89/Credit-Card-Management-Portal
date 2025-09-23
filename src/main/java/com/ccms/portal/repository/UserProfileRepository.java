package com.ccms.portal.repository;

import com.ccms.portal.model.UserEntity;
import com.ccms.portal.model.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {
    Optional<UserProfileEntity> findByUser(UserEntity user);
}
