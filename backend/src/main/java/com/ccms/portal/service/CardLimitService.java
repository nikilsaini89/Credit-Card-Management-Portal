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

        double requested = request.getNewCreditLimit();

        CreditCardEntity card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new CreditCardNotFoundException("Card not found for id: " + cardId));

        if (!isOwner(card, callerUsername)) {
            throw new UnauthorizedApplicationActionException("You are not allowed to update this card");
        }

        double minCardLimit = 0.0; // Default minimum limit
        double maxCardLimit = 1000000.0; // Default maximum limit

        if (requested < minCardLimit || requested > maxCardLimit) {
            throw new CardLimitException("Requested limit must be between " + minCardLimit + " and " + maxCardLimit);
        }

        double oldLimit = card.getCreditLimit().doubleValue();
        double available = card.getAvailableLimit().doubleValue();

        double outstanding = oldLimit - available;

        if (requested < outstanding) {
            throw new CardLimitException("New limit cannot be lower than outstanding balance (" + outstanding + ")");
        }

        double newAvailable = requested - outstanding;

        card.setCreditLimit(java.math.BigDecimal.valueOf(requested));
        card.setAvailableLimit(java.math.BigDecimal.valueOf(newAvailable));
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
}
