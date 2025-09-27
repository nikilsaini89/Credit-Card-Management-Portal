package com.ccms.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardBillResponse {
    private Long cardId;
    private String cardNumber;
    private String cardHolderName;
    private LocalDate statementDate;
    private LocalDate dueDate;
    private BigDecimal totalStatementAmount;
    private BigDecimal amountDue;
    private BigDecimal availableCredit;
    private BigDecimal creditLimit;
    private BigDecimal creditLimitUsage;
    private String spendingTrend;
    private String lastFour;
    private String network;
}
