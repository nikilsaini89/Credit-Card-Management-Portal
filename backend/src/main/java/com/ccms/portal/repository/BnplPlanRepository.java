package com.ccms.portal.repository;

import com.ccms.portal.entity.BnplPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BnplPlanRepository extends JpaRepository<BnplPlan, Long> {

  List<BnplPlan> findByUserId(Long userId);

  List<BnplPlan> findByUserIdAndIsActiveTrue(Long userId);

  List<BnplPlan> findByIsActiveTrue();

  @Query("SELECT b FROM BnplPlan b WHERE b.userId = :userId AND b.isActive = true")
  List<BnplPlan> findActivePlansByUserId(@Param("userId") Long userId);

  @Query("SELECT b FROM BnplPlan b WHERE b.userId = :userId AND b.status = :status")
  List<BnplPlan> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") BnplPlan.BnplStatus status);
}
