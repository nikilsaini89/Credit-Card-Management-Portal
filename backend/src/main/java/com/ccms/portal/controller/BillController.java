package com.ccms.portal.controller;

import com.ccms.portal.dto.response.CreditCardBillResponse;
import com.ccms.portal.service.CreditCardBillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BillController {

    private final CreditCardBillService billService;

    @GetMapping("/{userId}/{cardId}")
    public ResponseEntity<CreditCardBillResponse> getCreditCardBill(
            @PathVariable Long userId, 
            @PathVariable Long cardId) {
        log.info("Fetching credit card bill for user: {} and card: {}", userId, cardId);

        try {
            CreditCardBillResponse bill = billService.getCreditCardBill(userId, cardId);
            return ResponseEntity.ok(bill);
        } catch (Exception e) {
            log.error("Error fetching credit card bill for user {} and card {}: {}", userId, cardId, e.getMessage());
            return ResponseEntity.status(404).build();
        }
    }
}
