package com.ccms.portal.util;

import com.ccms.portal.dto.response.CreditCardResponse;
import com.ccms.portal.entity.CreditCardEntity;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Random;

@Component
public class CreditCardUtil {

    private static final int CARD_NUMBER_LENGTH = 16;

    public String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder(CARD_NUMBER_LENGTH);
        for (int index = 0; index < CARD_NUMBER_LENGTH; index++) {
            int digit = new Random().nextInt(10);
            cardNumber.append(digit);
        }
        return cardNumber.toString();
    }

    public LocalDate generateExpiryDate(int yearsFromNow) {
        return LocalDate.now().plusYears(yearsFromNow);
    }

    public CreditCardResponse buildCreditCardResponse(CreditCardEntity cardEntity){
        CreditCardResponse cardResponse = CreditCardResponse.builder()
                .id(cardEntity.getId())
                .cardHolderName(cardEntity.getUser().getName())
                .cardNumber(cardEntity.getCardNumber())
                .cardStatus(cardEntity.getCardStatus())
                .creditLimit(cardEntity.getCreditLimit().doubleValue())
                .availableLimit(cardEntity.getAvailableLimit().doubleValue())
                .expiryDate(cardEntity.getExpiryDate())
                .build();

        return cardResponse;
    }
}
