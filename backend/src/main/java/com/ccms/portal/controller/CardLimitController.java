package com.ccms.portal.controller;

import com.ccms.portal.dto.request.UpdateCardLimitRequest;
import com.ccms.portal.dto.response.CardLimitResponse;
import com.ccms.portal.service.CardLimitService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cards")
public class CardLimitController {
    private final CardLimitService cardLimitService;

    public CardLimitController(CardLimitService cardLimitService){
        this.cardLimitService = cardLimitService;
    }

    @PutMapping("/{cardId}/limit")
    public ResponseEntity<CardLimitResponse> updateLimit(@PathVariable Long cardId, @Valid @RequestBody UpdateCardLimitRequest request ) {
        CardLimitResponse response = cardLimitService.updateLimit(cardId, request);
        return ResponseEntity.ok(response);
    }
}
