package com.ccms.portal.util;

import com.ccms.portal.dto.response.CreditCardResponse;
import com.ccms.portal.entity.CardTypeEntity;
import com.ccms.portal.entity.CreditCardEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class CreditCardUtil {

    private static final Logger logger = LoggerFactory.getLogger(CreditCardUtil.class);
    private static final int CARD_NUMBER_LENGTH = 16;

    public String generateCardNumber() {
        logger.debug("Generating new card number");
        StringBuilder cardNumber = new StringBuilder(CARD_NUMBER_LENGTH);
        for (int index = 0; index < CARD_NUMBER_LENGTH; index++) {
            int digit = new Random().nextInt(10);
            cardNumber.append(digit);
        }
        return cardNumber.toString();
    }

    public Integer generateCvv(){
        logger.debug("Generating new CVV");
        return ThreadLocalRandom.current().nextInt(100, 1000);
    }

    public Date generateExpiryDate(int yearsFromNow) {
        logger.debug("Generating expiry date {} years from now", yearsFromNow);
        LocalDate expiry = LocalDate.now().plusYears(yearsFromNow);
        return Date.valueOf(expiry);
    }

    public CreditCardResponse buildCreditCardResponse(CreditCardEntity cardEntity){
        logger.debug("Building credit card response for card ID: {}", cardEntity.getId());

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
                .cvv(cardEntity.getCvv())
                .cardType(cardTypeInfo)
                .build();

        return cardResponse;
    }
}
