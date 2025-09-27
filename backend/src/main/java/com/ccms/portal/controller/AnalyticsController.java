package com.ccms.portal.controller;

import com.ccms.portal.dto.response.AnalyticsResponse;
import com.ccms.portal.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnalyticsController {

  private final AnalyticsService analyticsService;

  @GetMapping("/{userId}")
  public ResponseEntity<AnalyticsResponse> getAnalytics(@PathVariable Long userId) {
    log.info("Fetching analytics for user: {}", userId);

    try {
      AnalyticsResponse analytics = analyticsService.getAnalytics(userId);
      return ResponseEntity.ok(analytics);
    } catch (Exception e) {
      log.error("Error fetching analytics: {}", e.getMessage());
      return ResponseEntity.status(500).build();
    }
  }

  @GetMapping("/{userId}/categories")
  public ResponseEntity<List<AnalyticsResponse.CategoryBreakdown>> getCategoryBreakdown(@PathVariable Long userId) {
    log.info("Fetching category breakdown for user: {}", userId);

    try {
      List<AnalyticsResponse.CategoryBreakdown> breakdown = analyticsService.getCategoryBreakdown(userId);
      return ResponseEntity.ok(breakdown);
    } catch (Exception e) {
      log.error("Error fetching category breakdown: {}", e.getMessage());
      return ResponseEntity.status(500).build();
    }
  }

  @GetMapping("/{userId}/merchants")
  public ResponseEntity<List<AnalyticsResponse.TopMerchant>> getTopMerchants(@PathVariable Long userId) {
    log.info("Fetching top merchants for user: {}", userId);

    try {
      List<AnalyticsResponse.TopMerchant> merchants = analyticsService.getTopMerchants(userId);
      return ResponseEntity.ok(merchants);
    } catch (Exception e) {
      log.error("Error fetching top merchants: {}", e.getMessage());
      return ResponseEntity.status(500).build();
    }
  }

  @GetMapping("/{userId}/monthly")
  public ResponseEntity<List<Object[]>> getMonthlySpending(@PathVariable Long userId) {
    log.info("Fetching monthly spending for user: {}", userId);

    try {
      List<Object[]> monthlyData = analyticsService.getMonthlySpending(userId);
      return ResponseEntity.ok(monthlyData);
    } catch (Exception e) {
      log.error("Error fetching monthly spending: {}", e.getMessage());
      return ResponseEntity.status(500).build();
    }
  }
}
