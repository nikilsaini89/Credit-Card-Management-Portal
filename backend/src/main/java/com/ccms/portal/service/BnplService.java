package com.ccms.portal.service;

import com.ccms.portal.dto.response.BnplPlanResponse;
import com.ccms.portal.entity.BnplPlan;
import com.ccms.portal.entity.BnplPlan.BnplStatus;
import com.ccms.portal.exception.ResourceNotFoundException;
import com.ccms.portal.repository.BnplPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BnplService {

  private final BnplPlanRepository bnplPlanRepository;

  public List<BnplPlanResponse> getActiveBnplPlans(Long userId) {
    log.info("Fetching active BNPL plans for user: {}", userId);

    List<BnplPlan> plans = bnplPlanRepository.findActivePlansByUserId(userId);
    return plans.stream()
        .map(this::mapToBnplPlanResponse)
        .collect(Collectors.toList());
  }

  public List<BnplPlanResponse> getAllBnplPlans(Long userId) {
    log.info("Fetching all BNPL plans for user: {}", userId);

    List<BnplPlan> plans = bnplPlanRepository.findByUserId(userId);
    return plans.stream()
        .map(this::mapToBnplPlanResponse)
        .collect(Collectors.toList());
  }

  public BnplPlanResponse getBnplPlanById(Long planId) {
    log.info("Fetching BNPL plan by ID: {}", planId);

    BnplPlan plan = bnplPlanRepository.findById(planId)
        .orElseThrow(() -> new ResourceNotFoundException("BNPL plan not found with ID: " + planId));

    return mapToBnplPlanResponse(plan);
  }

  public BnplPlanResponse payBnplEmi(Long planId, BigDecimal amount) {
    log.info("Processing BNPL EMI payment for plan: {} with amount: {}", planId, amount);

    BnplPlan plan = bnplPlanRepository.findById(planId)
        .orElseThrow(() -> new ResourceNotFoundException("BNPL plan not found with ID: " + planId));

    if (!plan.getIsActive()) {
      throw new IllegalArgumentException("BNPL plan is not active");
    }

    if (plan.getStatus() == BnplStatus.COMPLETED) {
      throw new IllegalArgumentException("BNPL plan is already completed");
    }

    // Update paid amount
    BigDecimal newPaidAmount = plan.getPaidAmount().add(amount);
    plan.setPaidAmount(newPaidAmount);

    // Update paid months
    plan.setPaidMonths(plan.getPaidMonths() + 1);
    plan.setRemainingMonths(plan.getTenureMonths() - plan.getPaidMonths());

    // Check if plan is completed
    if (newPaidAmount.compareTo(plan.getTotalAmount()) >= 0) {
      plan.setStatus(BnplStatus.COMPLETED);
      plan.setIsActive(false);
      plan.setRemainingAmount(BigDecimal.ZERO);
    } else {
      plan.setRemainingAmount(plan.getTotalAmount().subtract(newPaidAmount));
      // Update next due date (add 1 month)
      plan.setNextDueDate(plan.getNextDueDate().plusMonths(1));
    }

    BnplPlan savedPlan = bnplPlanRepository.save(plan);
    log.info("BNPL EMI payment processed successfully");

    return mapToBnplPlanResponse(savedPlan);
  }

  public BnplPlanResponse createBnplPlan(Long userId, Long transactionId, String merchant,
      String category, BigDecimal totalAmount,
      Integer tenureMonths) {
    log.info("Creating BNPL plan for user: {} with amount: {}", userId, totalAmount);

    BnplPlan plan = new BnplPlan();
    plan.setUserId(userId);
    plan.setTransactionId(transactionId);
    plan.setMerchant(merchant);
    plan.setCategory(category);
    plan.setTotalAmount(totalAmount);
    plan.setPaidAmount(BigDecimal.ZERO);
    plan.setRemainingAmount(totalAmount);
    plan.setMonthlyEmi(totalAmount.divide(BigDecimal.valueOf(tenureMonths), 2, java.math.RoundingMode.UP));
    plan.setTenureMonths(tenureMonths);
    plan.setPaidMonths(0);
    plan.setRemainingMonths(tenureMonths);
    plan.setStatus(BnplStatus.ACTIVE);
    plan.setCreatedAt(LocalDateTime.now());
    plan.setNextDueDate(LocalDateTime.now().plusMonths(1).toLocalDate());
    plan.setIsActive(true);

    BnplPlan savedPlan = bnplPlanRepository.save(plan);
    log.info("BNPL plan created successfully with ID: {}", savedPlan.getId());

    return mapToBnplPlanResponse(savedPlan);
  }

  public void cancelBnplPlan(Long planId) {
    log.info("Cancelling BNPL plan: {}", planId);

    BnplPlan plan = bnplPlanRepository.findById(planId)
        .orElseThrow(() -> new ResourceNotFoundException("BNPL plan not found with ID: " + planId));

    if (plan.getStatus() == BnplStatus.COMPLETED) {
      throw new IllegalArgumentException("Cannot cancel completed BNPL plan");
    }

    plan.setStatus(BnplStatus.CANCELLED);
    plan.setIsActive(false);

    bnplPlanRepository.save(plan);
    log.info("BNPL plan cancelled successfully");
  }

  private BnplPlanResponse mapToBnplPlanResponse(BnplPlan plan) {
    // Calculate progress percentage
    double progressPercentage = 0.0;
    if (plan.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
      progressPercentage = plan.getPaidAmount()
          .divide(plan.getTotalAmount(), 4, java.math.RoundingMode.HALF_UP)
          .multiply(BigDecimal.valueOf(100))
          .doubleValue();
    }

    return BnplPlanResponse.builder()
        .id(plan.getId())
        .userId(plan.getUserId())
        .transactionId(plan.getTransactionId())
        .merchant(plan.getMerchant())
        .category(plan.getCategory())
        .totalAmount(plan.getTotalAmount())
        .paidAmount(plan.getPaidAmount())
        .remainingAmount(plan.getRemainingAmount())
        .monthlyEmi(plan.getMonthlyEmi())
        .tenureMonths(plan.getTenureMonths())
        .paidMonths(plan.getPaidMonths())
        .remainingMonths(plan.getRemainingMonths())
        .status(plan.getStatus())
        .createdAt(plan.getCreatedAt())
        .nextDueDate(plan.getNextDueDate())
        .isActive(plan.getIsActive())
        .progressPercentage(progressPercentage)
        .build();
  }
}
