package com.ccms.portal.service;

import com.ccms.portal.dto.response.AnalyticsResponse;
import com.ccms.portal.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

  private final TransactionRepository transactionRepository;

  public AnalyticsResponse getAnalytics(Long userId) {
    log.info("Fetching analytics for user: {}", userId);

    // Get category breakdown
    List<Object[]> categoryData = transactionRepository.getCategoryBreakdownByUserId(userId);
    List<AnalyticsResponse.CategoryBreakdown> categoryBreakdown = mapToCategoryBreakdown(categoryData);

    // Get top merchants
    List<Object[]> merchantData = transactionRepository.getTopMerchantsByUserId(userId);
    List<AnalyticsResponse.TopMerchant> topMerchants = mapToTopMerchants(merchantData);

    // Calculate monthly spending
    List<Object[]> monthlyData = transactionRepository.getMonthlySpendingByUserId(userId);
    BigDecimal thisMonth = calculateThisMonthSpending(monthlyData);
    BigDecimal lastMonth = calculateLastMonthSpending(monthlyData);
    double change = calculateChangePercentage(thisMonth, lastMonth);

    return AnalyticsResponse.builder()
        .thisMonth(thisMonth)
        .lastMonth(lastMonth)
        .change(change)
        .categoryBreakdown(categoryBreakdown)
        .topMerchants(topMerchants)
        .build();
  }

  public List<AnalyticsResponse.CategoryBreakdown> getCategoryBreakdown(Long userId) {
    log.info("Fetching category breakdown for user: {}", userId);

    List<Object[]> categoryData = transactionRepository.getCategoryBreakdownByUserId(userId);
    return mapToCategoryBreakdown(categoryData);
  }

  public List<AnalyticsResponse.TopMerchant> getTopMerchants(Long userId) {
    log.info("Fetching top merchants for user: {}", userId);

    List<Object[]> merchantData = transactionRepository.getTopMerchantsByUserId(userId);
    return mapToTopMerchants(merchantData);
  }

  public List<Object[]> getMonthlySpending(Long userId) {
    log.info("Fetching monthly spending for user: {}", userId);

    return transactionRepository.getMonthlySpendingByUserId(userId);
  }

  private List<AnalyticsResponse.CategoryBreakdown> mapToCategoryBreakdown(List<Object[]> categoryData) {
    List<AnalyticsResponse.CategoryBreakdown> breakdown = new ArrayList<>();

    // Calculate total amount for percentage calculation
    BigDecimal totalAmount = categoryData.stream()
        .map(data -> (BigDecimal) data[2])
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    for (Object[] data : categoryData) {
      String category = (String) data[0];
      Long count = (Long) data[1];
      BigDecimal amount = (BigDecimal) data[2];

      // Calculate percentage
      double percentage = 0.0;
      if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
        percentage = amount.divide(totalAmount, 4, BigDecimal.ROUND_HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
      }

      breakdown.add(AnalyticsResponse.CategoryBreakdown.builder()
          .name(category)
          .percentage(percentage)
          .amount(amount)
          .color(getCategoryColor(category))
          .build());
    }

    return breakdown;
  }

  private List<AnalyticsResponse.TopMerchant> mapToTopMerchants(List<Object[]> merchantData) {
    List<AnalyticsResponse.TopMerchant> merchants = new ArrayList<>();

    for (Object[] data : merchantData) {
      String merchant = (String) data[0];
      Long count = (Long) data[1];
      BigDecimal amount = (BigDecimal) data[2];

      merchants.add(AnalyticsResponse.TopMerchant.builder()
          .name(merchant)
          .count(count.intValue())
          .amount(amount)
          .build());
    }

    return merchants;
  }

  private String getCategoryColor(String category) {
    return switch (category.toLowerCase()) {
      case "shopping" -> "#ffd60a";
      case "food" -> "#f97316";
      case "travel" -> "#0b2540";
      case "entertainment" -> "#111827";
      case "utilities" -> "#6b7280";
      case "healthcare" -> "#10b981";
      case "education" -> "#8b5cf6";
      default -> "#6b7280";
    };
  }

  private BigDecimal calculateThisMonthSpending(List<Object[]> monthlyData) {
    LocalDateTime now = LocalDateTime.now();
    return monthlyData.stream()
        .filter(data -> {
          Integer year = (Integer) data[0];
          Integer month = (Integer) data[1];
          return year.equals(now.getYear()) && month.equals(now.getMonthValue());
        })
        .map(data -> (BigDecimal) data[2])
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal calculateLastMonthSpending(List<Object[]> monthlyData) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime lastMonth = now.minusMonths(1);
    return monthlyData.stream()
        .filter(data -> {
          Integer year = (Integer) data[0];
          Integer month = (Integer) data[1];
          return year.equals(lastMonth.getYear()) && month.equals(lastMonth.getMonthValue());
        })
        .map(data -> (BigDecimal) data[2])
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private double calculateChangePercentage(BigDecimal thisMonth, BigDecimal lastMonth) {
    if (lastMonth.compareTo(BigDecimal.ZERO) == 0) {
      return thisMonth.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
    }
    return thisMonth.subtract(lastMonth)
        .divide(lastMonth, 4, BigDecimal.ROUND_HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .doubleValue();
  }
}
