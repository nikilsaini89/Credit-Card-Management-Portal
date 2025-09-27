package com.ccms.portal.dto.response;

import com.ccms.portal.entity.BnplPlan.BnplStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BnplPlanResponse {

  private Long id;
  private Long userId;
  private Long transactionId;
  private String merchant;
  private String category;
  private BigDecimal totalAmount;
  private BigDecimal paidAmount;
  private BigDecimal remainingAmount;
  private BigDecimal monthlyEmi;
  private Integer tenureMonths;
  private Integer paidMonths;
  private Integer remainingMonths;
  private BnplStatus status;
  private LocalDateTime createdAt;
  private LocalDate nextDueDate;
  private boolean isActive;
  private double progressPercentage;
}
