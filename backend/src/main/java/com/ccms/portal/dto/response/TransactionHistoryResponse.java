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
public class TransactionHistoryResponse {

  private List<TransactionResponse> transactions;
  private long totalTransactions;
  private BigDecimal totalAmount;
  private BigDecimal totalSpent;
  private BigDecimal averageTransaction;
  private int bnplPlansCount;
  private int currentPage;
  private int totalPages;
  private boolean hasNext;
  private boolean hasPrevious;
}

