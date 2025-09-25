package com.ccms.portal.repository;

import com.ccms.portal.entity.CardApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CardApplicationRepository extends JpaRepository<CardApplicationEntity, Long> {

    List<CardApplicationEntity> findAllByUserId(Long userId);
}
