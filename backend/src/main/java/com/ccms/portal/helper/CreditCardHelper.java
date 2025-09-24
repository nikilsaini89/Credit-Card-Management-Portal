package com.ccms.portal.helper;

import com.ccms.portal.dto.response.CreditCardResponse;
import com.ccms.portal.model.CardTypeEntity;
import com.ccms.portal.model.CreditCardEntity;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Random;

@Component
public class CreditCardHelper {

    private static final int CARD_NUMBER_LENGTH = 16;

    public String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder(CARD_NUMBER_LENGTH);
        for (int index = 0; index < CARD_NUMBER_LENGTH; index++) {
            int digit = new Random().nextInt(10);
            cardNumber.append(digit);
        }
        return cardNumber.toString();
    }

    public Date generateExpiryDate(int yearsFromNow) {
        LocalDate expiry = LocalDate.now().plusYears(yearsFromNow);
        return Date.valueOf(expiry);
    }

    public CreditCardResponse buildCreditCardResponse(CreditCardEntity cardEntity){

        CardTypeEntity cardType = cardEntity.getCardType();

        CreditCardResponse.CardTypeInfo cardTypeInfo = CreditCardResponse.CardTypeInfo.builder()
                .name(cardType.getName())
                .networkType(cardType.getNetworkType())
                .description(cardType.getDescription())
                .build();

        CreditCardResponse cardResponse = CreditCardResponse.builder().
                id(cardEntity.getId())
                .cardHolderName(cardEntity.getUser().getName())
                .cardNumber(cardEntity.getCardNumber())
                .cardStatus(cardEntity.getCardStatus())
                .creditLimit(cardEntity.getCreditLimit())
                .availableLimit(cardEntity.getAvailableLimit())
                .expiryDate(cardEntity.getExpiryDate())
                .cardType(cardTypeInfo)
                .build();

        return cardResponse;
    }
}
