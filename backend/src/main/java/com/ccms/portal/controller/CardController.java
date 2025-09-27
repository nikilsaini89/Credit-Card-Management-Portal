package com.ccms.portal.controller;

import com.ccms.portal.dto.response.CreditCardResponse;
import com.ccms.portal.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CardController {

  private final CardService cardService;

  @GetMapping("/{userId}")
  public ResponseEntity<List<CreditCardResponse>> getCardsByUserId(@PathVariable Long userId) {
    log.info("Fetching cards for user: {}", userId);

    try {
      List<CreditCardResponse> cards = cardService.getAllCardsByUserId(userId);
      return ResponseEntity.ok(cards);
    } catch (Exception e) {
      log.error("Error fetching cards for user {}: {}", userId, e.getMessage());
      return ResponseEntity.status(500).build();
    }
  }

  @GetMapping("/card/{cardId}")
  public ResponseEntity<CreditCardResponse> getCardById(@PathVariable Long cardId) {
    log.info("Fetching card by ID: {}", cardId);

    try {
      CreditCardResponse card = cardService.getCardById(cardId);
      return ResponseEntity.ok(card);
    } catch (Exception e) {
      log.error("Error fetching card by ID {}: {}", cardId, e.getMessage());
      return ResponseEntity.status(404).build();
    }
  }
}
