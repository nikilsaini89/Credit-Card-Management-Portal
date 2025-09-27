package com.ccms.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {

  private BigDecimal thisMonth;
  private BigDecimal lastMonth;
  private double change;
  private List<CategoryBreakdown> categoryBreakdown;
  private List<TopMerchant> topMerchants;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CategoryBreakdown {
    private String name;
    private double percentage;
    private BigDecimal amount;
    private String color;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TopMerchant {
    private String name;
    private int count;
    private BigDecimal amount;
  }
}

