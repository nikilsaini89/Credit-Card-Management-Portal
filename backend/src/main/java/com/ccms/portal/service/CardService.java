package com.ccms.portal.service;

import com.ccms.portal.dto.request.CreateCardRequest;
import com.ccms.portal.dto.request.UpdateCardStatusRequest;
import com.ccms.portal.dto.response.CreditCardResponse;
import com.ccms.portal.enums.CardStatus;
import com.ccms.portal.exception.CreditCardNotFoundException;
import com.ccms.portal.exception.UserNotFoundException;
import com.ccms.portal.util.CreditCardUtil;
import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.entity.UserEntity;
import com.ccms.portal.entity.Bank;
import com.ccms.portal.repository.CreditCardRepository;
import com.ccms.portal.repository.UserRepository;
import com.ccms.portal.repository.BankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CardService {

        @Autowired
        CreditCardRepository cardRepository;

        @Autowired
        UserRepository userRepository;

        @Autowired
        BankRepository bankRepository;

        @Autowired
        CreditCardUtil cardHelper;

        public List<CreditCardResponse> getAllCardsByUserId(Long userId) {
                UserEntity user = userRepository.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

                return cardRepository.findByUserId(userId).stream()
                                .map(cardHelper::buildCreditCardResponse)
                                .toList();
        }

        public CreditCardResponse createCard(CreateCardRequest cardRequest) {
                UserEntity user = userRepository.findById(cardRequest.getUserId())
                                .orElseThrow(() -> new UserNotFoundException(
                                                "User not found with ID: " + cardRequest.getUserId()));

                Bank bank = bankRepository.findById(cardRequest.getBankId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Bank not found with ID: " + cardRequest.getBankId()));

                CreditCardEntity entity = CreditCardEntity.builder()
                                .user(user)
                                .issuingBank(bank)
                                .creditLimit(BigDecimal.valueOf(cardRequest.getCreditLimit()))
                                .availableLimit(BigDecimal.valueOf(cardRequest.getCreditLimit()))
                                .cardStatus(CardStatus.ACTIVE)
                                .cardNumber(cardHelper.generateCardNumber())
                                .expiryDate(cardHelper.generateExpiryDate(5))
                                .status("ACTIVE")
                                .build();

                CreditCardEntity saved = cardRepository.save(entity);
                return cardHelper.buildCreditCardResponse(saved);
        }

        public CreditCardResponse updateCardStatus(UpdateCardStatusRequest cardStatusRequest, Long cardId) {
                CreditCardEntity creditCard = cardRepository.findById(cardId)
                                .orElseThrow(() -> new CreditCardNotFoundException(
                                                "Credit Card not found with card Id: " + cardId));

                creditCard.setCardStatus(cardStatusRequest.getCardStatus());
                CreditCardEntity savedEntity = cardRepository.save(creditCard);
                return cardHelper.buildCreditCardResponse(savedEntity);
        }

        public CreditCardResponse getCardById(Long cardId) {
                CreditCardEntity creditCard = cardRepository.findById(cardId)
                                .orElseThrow(() -> new CreditCardNotFoundException(
                                                "Credit Card not found with card Id: " + cardId));

                return cardHelper.buildCreditCardResponse(creditCard);
        }
}
