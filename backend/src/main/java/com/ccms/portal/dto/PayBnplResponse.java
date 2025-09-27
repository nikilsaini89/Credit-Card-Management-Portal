package com.ccms.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayBnplResponse {
  private boolean success;
  private String message;
  private String paymentId;
  private BigDecimal amountPaid;
  private BigDecimal remainingAmount;
  private int remainingInstallments;
  private LocalDateTime nextDueDate;
  private String status;
}
