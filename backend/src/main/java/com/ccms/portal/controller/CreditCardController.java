package com.ccms.portal.controller;

import com.ccms.portal.dto.request.CreateCardRequest;
import com.ccms.portal.dto.request.UpdateCardStatusRequest;
import com.ccms.portal.dto.response.CreditCardResponse;
import com.ccms.portal.entity.CardTypeEntity;
import com.ccms.portal.service.CardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
public class CreditCardController {

    private static final Logger logger = LoggerFactory.getLogger(CreditCardController.class);

    @Autowired
    private CardService cardService;

    @GetMapping
    public ResponseEntity<?> getCards() {
        logger.info("GET /cards - Fetching user cards");
        
        List<CreditCardResponse> userCards = cardService.getAllCardsByUserId();
        if (userCards.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(userCards);
    }


    @PutMapping("/{cardId}/status")
    public ResponseEntity<?> updateCardStatus(@RequestBody UpdateCardStatusRequest cardStatusRequest, @PathVariable Long cardId) {
        logger.info("PUT /cards/{}/status - Updating card status to {}", cardId, cardStatusRequest.getCardStatus());
        
        CreditCardResponse updatedCard = cardService.updateCardStatus(cardStatusRequest, cardId);
        return ResponseEntity.ok(updatedCard);
    }

    @GetMapping("/type")
    public ResponseEntity<?> getCardTypes() {
        logger.info("GET /cards/type - Fetching card types");
        
        List<CardTypeEntity> allCardType = cardService.getCardTypes();
        return ResponseEntity.ok(allCardType);
    }
}
