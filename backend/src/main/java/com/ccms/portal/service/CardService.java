package com.ccms.portal.service;

import com.ccms.portal.dto.request.CreateCardRequest;
import com.ccms.portal.dto.request.UpdateCardStatusRequest;
import com.ccms.portal.dto.response.CreditCardResponse;
import com.ccms.portal.enums.CardStatus;
import com.ccms.portal.exception.CardTypeNotFoundException;
import com.ccms.portal.exception.CreditCardNotFoundException;
import com.ccms.portal.exception.UnauthorizedApplicationActionException;
import com.ccms.portal.exception.UserNotFoundException;
import com.ccms.portal.util.CreditCardUtil;
import com.ccms.portal.entity.CardTypeEntity;
import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.entity.UserEntity;
import com.ccms.portal.repository.CreditCardRepository;
import com.ccms.portal.repository.CardTypeRepository;
import com.ccms.portal.repository.UserRepository;
import com.ccms.portal.util.JwtUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
