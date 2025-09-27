package com.ccms.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;
    private Long cardId;
    private String merchantName;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String category;
    private Boolean isBnpl;
    private String cardType;
    private String lastFour;
    private String status;
    private LocalDateTime createdAt;
}
