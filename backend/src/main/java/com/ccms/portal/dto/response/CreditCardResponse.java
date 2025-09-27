package com.ccms.portal.dto.response;

import com.ccms.portal.enums.CardStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class CreditCardResponse {
    private Long id;
    private String cardHolderName;
    private String cardNumber;
    private CardStatus cardStatus;
    private Double creditLimit;
    private Double availableLimit;
    private LocalDate expiryDate;
}
