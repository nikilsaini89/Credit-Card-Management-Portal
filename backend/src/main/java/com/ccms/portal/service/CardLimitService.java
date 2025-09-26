package com.ccms.portal.service;

import com.ccms.portal.dto.request.UpdateCardLimitRequest;
import com.ccms.portal.dto.response.CardLimitResponse;
import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.exception.CardLimitException;
import com.ccms.portal.exception.CreditCardNotFoundException;
import com.ccms.portal.exception.UnauthorizedApplicationActionException;
import com.ccms.portal.repository.CreditCardRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CardLimitService {
    private final CreditCardRepository creditCardRepository;

    public CardLimitService(CreditCardRepository creditCardRepository) {
        this.creditCardRepository = creditCardRepository;
    }

    @Transactional
    public CardLimitResponse updateLimit(Long cardId, UpdateCardLimitRequest request, String callerUsername) {
        if (request == null || request.getNewCreditLimit() == null) {
            throw new CardLimitException("Request or new credit limit must not be null");
        }

        BigDecimal requested = safe(request.getNewCreditLimit());

        CreditCardEntity card = creditCardRepository.findById(cardId).orElseThrow(() -> new CreditCardNotFoundException("Card not found for id: " + cardId));

        if (!isOwner(card, callerUsername)) {
            throw new UnauthorizedApplicationActionException("You are not allowed to update this card");
        }

        BigDecimal minCardLimit = safe(BigDecimal.valueOf(card.getCardType().getMinCardLimit()));
        BigDecimal maxCardLimit = safe(BigDecimal.valueOf(card.getCardType().getMaxCardLimit()));

        if (requested.compareTo(minCardLimit) < 0 || requested.compareTo(maxCardLimit) > 0) {
            throw new CardLimitException("Requested limit must be between " + minCardLimit + " and " + maxCardLimit);
        }

        BigDecimal oldLimit = safe(BigDecimal.valueOf(card.getCreditLimit()));
        BigDecimal available = safe(BigDecimal.valueOf(card.getAvailableLimit()));

        BigDecimal outstanding = oldLimit.subtract(available);

        if (requested.compareTo(outstanding) < 0) {
            throw new CardLimitException("New limit cannot be lower than outstanding balance (" + outstanding + ")");
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
    private boolean isOwner(CreditCardEntity card, String callerUsername) {
        if (callerUsername == null || card.getUser() == null) return false;
        String ownerEmail = card.getUser().getEmail();
        return ownerEmail != null && ownerEmail.equalsIgnoreCase(callerUsername);
    }

    private BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
