package com.ccms.portal.controller;

import com.ccms.portal.dto.response.BnplPlanResponse;
import com.ccms.portal.service.BnplService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/bnpl")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BnplController {

  private final BnplService bnplService;

  @GetMapping("/active/{userId}")
  public ResponseEntity<List<BnplPlanResponse>> getActiveBnplPlans(@PathVariable Long userId) {
    log.info("Fetching active BNPL plans for user: {}", userId);

    try {
      List<BnplPlanResponse> plans = bnplService.getActiveBnplPlans(userId);
      return ResponseEntity.ok(plans);
    } catch (Exception e) {
      log.error("Error fetching active BNPL plans: {}", e.getMessage());
      return ResponseEntity.status(500).build();
    }
  }

  @GetMapping("/{userId}")
  public ResponseEntity<List<BnplPlanResponse>> getAllBnplPlans(@PathVariable Long userId) {
    log.info("Fetching all BNPL plans for user: {}", userId);

    try {
      List<BnplPlanResponse> plans = bnplService.getAllBnplPlans(userId);
      return ResponseEntity.ok(plans);
    } catch (Exception e) {
      log.error("Error fetching BNPL plans: {}", e.getMessage());
      return ResponseEntity.status(500).build();
    }
  }

  @GetMapping("/plan/{planId}")
  public ResponseEntity<BnplPlanResponse> getBnplPlanById(@PathVariable Long planId) {
    log.info("Fetching BNPL plan by ID: {}", planId);

    try {
      BnplPlanResponse plan = bnplService.getBnplPlanById(planId);
      return ResponseEntity.ok(plan);
    } catch (Exception e) {
      log.error("Error fetching BNPL plan: {}", e.getMessage());
      return ResponseEntity.status(404).build();
    }
  }

  @PostMapping("/plan/{planId}/pay")
  public ResponseEntity<BnplPlanResponse> payBnplEmi(@PathVariable Long planId,
      @RequestParam BigDecimal amount) {
    log.info("Processing BNPL EMI payment for plan: {} with amount: {}", planId, amount);

    try {
      BnplPlanResponse plan = bnplService.payBnplEmi(planId, amount);
      return ResponseEntity.ok(plan);
    } catch (Exception e) {
      log.error("Error processing BNPL payment: {}", e.getMessage());
      return ResponseEntity.status(400).build();
    }
  }

  @PostMapping("/create")
  public ResponseEntity<BnplPlanResponse> createBnplPlan(@RequestParam Long userId,
      @RequestParam(required = false) Long transactionId,
      @RequestParam String merchant,
      @RequestParam String category,
      @RequestParam BigDecimal totalAmount,
      @RequestParam Integer tenureMonths) {
    log.info("Creating BNPL plan for user: {} with amount: {}", userId, totalAmount);

    try {
      // Auto-generate transaction ID if not provided
      if (transactionId == null) {
        transactionId = System.currentTimeMillis(); // Simple auto-generation using timestamp
        log.info("Auto-generated transaction ID: {}", transactionId);
      }

      BnplPlanResponse plan = bnplService.createBnplPlan(userId, transactionId, merchant,
          category, totalAmount, tenureMonths);
      return ResponseEntity.ok(plan);
    } catch (Exception e) {
      log.error("Error creating BNPL plan: {}", e.getMessage());
      return ResponseEntity.status(400).build();
    }
  }

  @PostMapping("/plan/{planId}/cancel")
  public ResponseEntity<Void> cancelBnplPlan(@PathVariable Long planId) {
    log.info("Cancelling BNPL plan: {}", planId);

    try {
      bnplService.cancelBnplPlan(planId);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      log.error("Error cancelling BNPL plan: {}", e.getMessage());
      return ResponseEntity.status(400).build();
    }
  }
}
