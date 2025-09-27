package com.ccms.portal.dto.response;

import com.ccms.portal.entity.Transaction.TransactionStatus;
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
public class TransactionResponse {

  private Long id;
  private Long serialNo;
  private Long cardId;
  private String cardType;
  private String lastFour;
  private Long merchantAccountId;
  private String merchant;
  private String category;
  private BigDecimal amount;
  private TransactionStatus status;
  private Boolean isBnpl;
  private String network;
  private LocalDateTime createdAt;
  private String transactionId; // For frontend display
}
