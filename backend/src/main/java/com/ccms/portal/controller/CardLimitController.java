package com.ccms.portal.controller;

import com.ccms.portal.dto.request.UpdateCardLimitRequest;
import com.ccms.portal.dto.response.CardLimitResponse;
import com.ccms.portal.exception.UnauthorizedApplicationActionException;
import com.ccms.portal.service.AuthService;
import com.ccms.portal.service.CardLimitService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cards")
public class CardLimitController {
    private final CardLimitService cardLimitService;
    private final AuthService authService;

    public CardLimitController(CardLimitService cardLimitService, AuthService authService){
        this.cardLimitService = cardLimitService;
        this.authService = authService;
    }

    @PutMapping("/{cardId}/limit")
    public ResponseEntity<CardLimitResponse> updateLimit(@PathVariable Long cardId, @Valid @RequestBody UpdateCardLimitRequest request, Authentication authentication) {
        String email = authentication == null ? null : authentication.getName();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedApplicationActionException("Not authenticated");
        }
        authService.verifyPasswordForEmail(email, request.getPassword());
        CardLimitResponse response = cardLimitService.updateLimit(cardId, request);
        return ResponseEntity.ok(response);
    }
}
