package com.ccms.portal.service;

import com.ccms.portal.dto.request.UpdateCardLimitRequest;
import com.ccms.portal.dto.response.CardLimitResponse;
import com.ccms.portal.entity.CardTypeEntity;
import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.exception.CardLimitException;
import com.ccms.portal.repository.CardTypeRepository;
import com.ccms.portal.repository.CreditCardRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CardLimitService {

    private final CreditCardRepository creditCardRepository;
    private final CardTypeRepository cardTypeRepository;

    public CardLimitService(CreditCardRepository creditCardRepository,
                            CardTypeRepository cardTypeRepository) {
        this.creditCardRepository = creditCardRepository;
        this.cardTypeRepository = cardTypeRepository;
    }

    @Transactional
    public CardLimitResponse updateLimit(Long cardId, UpdateCardLimitRequest request) {
        BigDecimal requested = nonNull(request.getNewCreditLimit());

        CreditCardEntity card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new CardLimitException("Card not found for id: " + cardId));

        BigDecimal oldLimit = nonNull(BigDecimal.valueOf(card.getCreditLimit()));
        BigDecimal available = nonNull(BigDecimal.valueOf(card.getAvailableLimit()));

        CardTypeEntity product = cardTypeRepository.findById(card.getCardType().getId())
                .orElseThrow(() -> new CardLimitException("Card product not found"));

        BigDecimal minLimit = nonNull(BigDecimal.valueOf(product.getMinCardLimit()));
        BigDecimal maxLimit = nonNull(BigDecimal.valueOf(product.getMaxCardLimit()));

        if (requested.compareTo(minLimit) < 0 || requested.compareTo(maxLimit) > 0) {
            throw new CardLimitException("Requested limit is outside allowed range");
        }

        BigDecimal outstanding = oldLimit.subtract(available);
        if (requested.compareTo(outstanding) < 0) {
            throw new CardLimitException("New limit cannot be lower than outstanding balance");
        }

        BigDecimal newAvailable = requested.subtract(outstanding);

        card.setCreditLimit(requested.doubleValue());
        card.setAvailableLimit(newAvailable.doubleValue());
        creditCardRepository.save(card);

        return CardLimitResponse.builder()
                .cardId(card.getId())
                .oldLimit(oldLimit)
                .newLimit(requested)
                .availableLimit(newAvailable)
                .build();
    }

    private BigDecimal nonNull(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
