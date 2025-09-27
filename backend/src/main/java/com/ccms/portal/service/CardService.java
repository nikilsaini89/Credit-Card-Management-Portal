package com.ccms.portal.service;

import com.ccms.portal.dto.request.CreateCardRequest;
import com.ccms.portal.dto.request.UpdateCardStatusRequest;
import com.ccms.portal.dto.response.CardDetailResponse;
import com.ccms.portal.dto.response.CardResponse;
import com.ccms.portal.dto.response.CreditCardResponse;
import com.ccms.portal.dto.response.TransactionResponse;
import com.ccms.portal.enums.CardStatus;
import com.ccms.portal.exception.CardTypeNotFoundException;
import com.ccms.portal.exception.CreditCardNotFoundException;
import com.ccms.portal.exception.UnauthorizedApplicationActionException;
import com.ccms.portal.exception.UserNotFoundException;
import com.ccms.portal.util.CreditCardUtil;
import com.ccms.portal.entity.CardTypeEntity;
import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.entity.Transaction;
import com.ccms.portal.entity.UserEntity;
import com.ccms.portal.repository.CreditCardRepository;
import com.ccms.portal.repository.CardTypeRepository;
import com.ccms.portal.repository.TransactionRepository;
import com.ccms.portal.repository.UserRepository;
import com.ccms.portal.util.JwtUserDetails;
import com.ccms.portal.util.MaskUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class CardService {

    @Autowired
    CreditCardRepository cardRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CardTypeRepository cardTypeRepository;

    @Autowired
    CreditCardUtil cardHelper;

    @Autowired
    TransactionRepository transactionRepository;

    public List<CreditCardResponse> getAllCardsByUserId(){
        JwtUserDetails currentUser = (JwtUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        final Long userId = currentUser.getUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        return cardRepository.findAllByUserIdWithCardType(userId).stream()
                .map(cardHelper::buildCreditCardResponse)
                .toList();
    }

    public CreditCardResponse createCard(CreateCardRequest cardRequest){
        UserEntity user = userRepository.findById(cardRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + cardRequest.getUserId()));

        CardTypeEntity cardType = cardTypeRepository.findById(cardRequest.getCardTypeId())
                .orElseThrow(() -> new CardTypeNotFoundException("Card type not found with ID: " + cardRequest.getCardTypeId()));

        CreditCardEntity entity = CreditCardEntity.builder()
                .user(user)
                .cardType(cardType)
                .creditLimit(cardRequest.getCreditLimit())
                .availableLimit(cardRequest.getCreditLimit())
                .cardStatus(CardStatus.ACTIVE)
                .cardNumber(cardHelper.generateCardNumber())
                .cvv(cardHelper.generateCvv())
                .expiryDate(cardHelper.generateExpiryDate(5))
                .build();

        CreditCardEntity saved = cardRepository.save(entity);
        return cardHelper.buildCreditCardResponse(saved);
    }

    public CreditCardResponse updateCardStatus(UpdateCardStatusRequest cardStatusRequest, Long cardId){
        JwtUserDetails currentUser = (JwtUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        CreditCardEntity creditCard = cardRepository.findById(cardId)
                .orElseThrow(() -> new CreditCardNotFoundException("Credit Card not found with card Id: " + cardId));

        if(creditCard.getUser() == null || !currentUser.getUserId().equals(creditCard.getUser().getId())){
            throw new UnauthorizedApplicationActionException("You are not allowed to update the status of this card");
        }
        creditCard.setCardStatus(cardStatusRequest.getCardStatus());
        CreditCardEntity savedEntity = cardRepository.save(creditCard);
        return cardHelper.buildCreditCardResponse(savedEntity);
    }

    @Transactional
    public CardDetailResponse getCardDetail(Long cardId) {
        CreditCardEntity cardEntity = cardRepository.findById(cardId).orElse(null);
        if (cardEntity == null) return null;

        CreditCardResponse creditCardResp = cardHelper.buildCreditCardResponse(cardEntity);

        // INSERTED: mask card number and remove cvv before sending to client
        creditCardResp = CreditCardResponse.builder()
                .id(creditCardResp.getId())
                .cardHolderName(creditCardResp.getCardHolderName())
                .cardNumber(MaskUtil.maskCardNumber(cardEntity.getCardNumber()))
                .cardStatus(creditCardResp.getCardStatus())
                .creditLimit(creditCardResp.getCreditLimit())
                .availableLimit(creditCardResp.getAvailableLimit())
                .expiryDate(creditCardResp.getExpiryDate())
                .cvv(null)
                .cardType(creditCardResp.getCardType())
                .build();
        // END INSERT

        List<Transaction> txEntities = transactionRepository.findTop10ByCard_IdOrderByCreatedAtDesc(cardEntity.getId());
        List<TransactionResponse> txDtos = txEntities.stream().map(t -> {
            TransactionResponse tr = new TransactionResponse();
            tr.setId(t.getId() == null ? null : t.getId().toString());
            tr.setCardId(t.getCard() == null || t.getCard().getId() == null ? null : t.getCard().getId().toString());
            tr.setAmount(t.getAmount() == null ? 0.0 : t.getAmount().doubleValue());
            tr.setMerchant(t.getMerchantAccount() != null ? String.valueOf(t.getMerchantAccount()) : null);
            tr.setCategory(t.getNetwork());
            tr.setMode(t.getProcessor() != null ? String.valueOf(t.getProcessor()) : null);
            if (t.getCreatedAt() != null) {
                tr.setDate(t.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
            } else {
                tr.setDate(null);
            }
            return tr;
        }).collect(Collectors.toList());
        return CardDetailResponse.builder().card(creditCardResp).transactions(txDtos).build();
    }

    @Transactional
    public List<CardResponse> getCardsForCurrentUser() {
        JwtUserDetails currentUser = (JwtUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = currentUser.getUserId();
        List<CreditCardEntity> cards = cardRepository.findAllByUserIdWithCardType(userId);
        return cards.stream().map(c -> {
            CardResponse cr = new CardResponse();
            cr.setId(c.getId() == null ? null : c.getId().toString());
            cr.setName(c.getCardType() != null ? c.getCardType().getName() : "Card");
            // INSERTED: use MaskUtil to mask PAN for dashboard list
            cr.setNumber(MaskUtil.maskCardNumber(c.getCardNumber()));
            // END INSERT
            cr.setTotalLimit(c.getCreditLimit() == null ? 0.0 : c.getCreditLimit());
            cr.setAvailableLimit(c.getAvailableLimit() == null ? 0.0 : c.getAvailableLimit());
            cr.setStatus(c.getCardStatus() == null ? null : c.getCardStatus().name());
            return cr;
        }).collect(Collectors.toList());
    }

}