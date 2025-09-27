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

    @GetMapping
    public ResponseEntity<?> getCards()    {
        List<CreditCardResponse> userCards = cardService.getAllCardsByUserId();
        if (userCards.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(userCards);
    }


    @PutMapping("/{cardId}/status")
    public ResponseEntity<?> updateCardStatus(@RequestBody UpdateCardStatusRequest cardStatusRequest, @PathVariable Long cardId){
        CreditCardResponse updatedCard = cardService.updateCardStatus(cardStatusRequest, cardId);
        return ResponseEntity.ok(updatedCard);
    }

}
