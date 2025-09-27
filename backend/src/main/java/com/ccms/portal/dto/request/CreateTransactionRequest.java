package com.ccms.portal.dto.request;

import com.ccms.portal.entity.Transaction.TransactionStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {

  @NotNull(message = "Card ID is required")
  private Long cardId;

  @NotNull(message = "Merchant account ID is required")
  private Long merchantAccountId;

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
  @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 integer digits and 2 decimal places")
  private BigDecimal amount;

  @NotBlank(message = "Merchant name is required")
  @Size(max = 100, message = "Merchant name must not exceed 100 characters")
  private String merchant;

  @NotBlank(message = "Category is required")
  @Size(max = 50, message = "Category must not exceed 50 characters")
  private String category;

  @NotNull(message = "Status is required")
  private TransactionStatus status;

  @NotNull(message = "BNPL flag is required")
  private Boolean isBnpl;

  private Integer tenureMonths; // For BNPL transactions

  @Size(max = 50, message = "Card type must not exceed 50 characters")
  private String cardType;

  @Size(max = 4, message = "Last four digits must not exceed 4 characters")
  private String lastFour;

  @Size(max = 30, message = "Network must not exceed 30 characters")
  private String network;
}
