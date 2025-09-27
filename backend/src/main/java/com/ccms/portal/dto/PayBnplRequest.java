package com.ccms.portal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayBnplRequest {
  @NotNull(message = "BNPL plan ID is required")
  private Long bnplPlanId;

  @NotNull(message = "Payment amount is required")
  @Positive(message = "Payment amount must be positive")
  private BigDecimal amount;

  private String paymentMethod = "CARD"; // Default to card payment
  private String notes;
}
