package com.ccms.portal.service;

import com.ccms.portal.dto.request.UpdateCardLimitRequest;
import com.ccms.portal.dto.response.CardLimitResponse;
import com.ccms.portal.entity.CardTypeEntity;
import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.exception.CardLimitException;
import com.ccms.portal.repository.CardTypeRepository;
import com.ccms.portal.repository.CreditCardRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CardLimitService {

    private static final Logger log = LoggerFactory.getLogger(CardLimitService.class);

    private final CreditCardRepository creditCardRepository;
    private final CardTypeRepository cardTypeRepository;

    public CardLimitService(CreditCardRepository creditCardRepository,
                            CardTypeRepository cardTypeRepository) {
        this.creditCardRepository = creditCardRepository;
        this.cardTypeRepository = cardTypeRepository;
    }

    @Transactional
    public CardLimitResponse updateLimit(Long cardId, UpdateCardLimitRequest request) {
        if (request == null || request.getNewCreditLimit() == null) {
            throw new CardLimitException("Request or new credit limit must not be null");
        }

        BigDecimal requested = nonNull(request.getNewCreditLimit());

        CreditCardEntity card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new CardLimitException("Card not found for id: " + cardId));

        if (card.getCardType() == null || card.getCardType().getId() == null) {
            throw new CardLimitException("Card product not associated");
        }

        BigDecimal oldLimit = nonNull(BigDecimal.valueOf(card.getCreditLimit()));
        BigDecimal available = nonNull(BigDecimal.valueOf(card.getAvailableLimit()));

        if (available.compareTo(oldLimit) > 0) {
            log.error("Data inconsistency for cardId {}: available ({}) > credit ({})", cardId, available, oldLimit);
            throw new CardLimitException("Data inconsistency: available limit (" + available
                    + ") is greater than credit limit (" + oldLimit + ")");
        }

        CardTypeEntity product = cardTypeRepository.findById(card.getCardType().getId())
                .orElseThrow(() -> new CardLimitException("Card product not found"));

        BigDecimal minLimit = nonNull(BigDecimal.valueOf(product.getMinCardLimit()));
        BigDecimal maxLimit = nonNull(BigDecimal.valueOf(product.getMaxCardLimit()));

        if (requested.compareTo(minLimit) < 0 || requested.compareTo(maxLimit) > 0) {
            throw new CardLimitException("Requested limit is outside allowed range");
        }

        BigDecimal outstanding = oldLimit.subtract(available);

        if (outstanding.signum() < 0) {
            log.warn("Negative outstanding detected for cardId {}: outstanding = {}. Normalizing to 0.", cardId, outstanding);
            outstanding = BigDecimal.ZERO;
        }

        if (requested.compareTo(outstanding) < 0) {
            throw new CardLimitException("New limit cannot be lower than outstanding balance");
        }

        BigDecimal newAvailable = requested.subtract(outstanding);

        if (newAvailable.signum() < 0) {
            log.error("Computed newAvailable negative for cardId {}: newAvailable = {}. Aborting.", cardId, newAvailable);
            throw new CardLimitException("Computed available limit is invalid");
        }

        card.setCreditLimit(requested.doubleValue());
        card.setAvailableLimit(newAvailable.doubleValue());
        creditCardRepository.save(card);

        log.info("Updated cardId {}: oldLimit={}, newLimit={}, available={}", cardId, oldLimit, requested, newAvailable);

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
