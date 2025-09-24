package com.ccms.portal.controller;


import com.ccms.portal.dto.request.CreateCardRequest;
import com.ccms.portal.dto.request.UpdateCardStatusRequest;
import com.ccms.portal.dto.response.CreditCardResponse;
import com.ccms.portal.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
public class CreditCardController {

    @Autowired
    private CardService cardService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getCards(@PathVariable Long userId)    {
        List<CreditCardResponse> userCards = cardService.getAllCardsByUserId(userId);
        if (userCards.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(userCards);
    }


    @PostMapping
    public ResponseEntity<?> createCard(@RequestBody CreateCardRequest request) {
        CreditCardResponse createdCard = cardService.createCard(request);
        return new ResponseEntity<>(createdCard,HttpStatus.CREATED);
    }

    @PutMapping("/{cardId}/status")
    public ResponseEntity<?> updateCardStatus(@RequestBody UpdateCardStatusRequest cardStatusRequest, @PathVariable Long cardId){
        CreditCardResponse updatedCard = cardService.updateCardStatus(cardStatusRequest, cardId);
        return ResponseEntity.ok(updatedCard);
    }

}
