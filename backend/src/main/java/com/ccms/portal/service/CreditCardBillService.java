package com.ccms.portal.service;

import com.ccms.portal.dto.response.CreditCardBillResponse;
import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.exception.ResourceNotFoundException;
import com.ccms.portal.repository.CreditCardRepository;
import com.ccms.portal.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditCardBillService {

    private final CreditCardRepository creditCardRepository;
    private final TransactionRepository transactionRepository;

    public CreditCardBillResponse getCreditCardBill(Long userId, Long cardId) {
        log.info("Fetching credit card bill for user: {} and card: {}", userId, cardId);

        // Get credit card details
        CreditCardEntity card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit card not found with ID: " + cardId));

        // Verify card belongs to user
        if (!card.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Credit card does not belong to user: " + userId);
        }

        // Calculate current month spending
        BigDecimal currentMonthSpending = transactionRepository.getCurrentMonthSpendingByCardId(cardId);
        
        // Calculate last month spending
        LocalDate now = LocalDate.now();
        LocalDate lastMonth = now.minusMonths(1);
        BigDecimal lastMonthSpending = transactionRepository.getLastMonthSpendingByCardId(cardId, 
                lastMonth.getYear(), lastMonth.getMonthValue());

        // Calculate credit limit usage
        BigDecimal creditLimit = card.getCreditLimit();
        BigDecimal availableCredit = card.getAvailableLimit();
        BigDecimal usedAmount = creditLimit.subtract(availableCredit);
        BigDecimal usagePercentage = usedAmount.divide(creditLimit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        // Calculate spending trend
        String spendingTrend = calculateSpendingTrend(currentMonthSpending, lastMonthSpending);

        // Generate statement and due dates
        LocalDate statementDate = LocalDate.now().withDayOfMonth(14); // 14th of current month
        LocalDate dueDate = statementDate.plusDays(25); // 25 days after statement date

        return CreditCardBillResponse.builder()
                .cardId(card.getId())
                .cardNumber(maskCardNumber(card.getCardNumber()))
                .cardHolderName(card.getUser().getName())
                .statementDate(statementDate)
                .dueDate(dueDate)
                .totalStatementAmount(currentMonthSpending)
                .amountDue(currentMonthSpending)
                .availableCredit(availableCredit)
                .creditLimit(creditLimit)
                .creditLimitUsage(usagePercentage)
                .spendingTrend(spendingTrend)
                .lastFour(card.getCardNumber().substring(card.getCardNumber().length() - 4))
                .network("Visa") // You can get this from card entity if available
                .build();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return "****";
        }
        String firstFour = cardNumber.substring(0, 4);
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return firstFour + " **** **** " + lastFour;
    }

    private String calculateSpendingTrend(BigDecimal current, BigDecimal last) {
        if (last.compareTo(BigDecimal.ZERO) == 0) {
            return "First month spending";
        }
        
        BigDecimal difference = current.subtract(last);
        BigDecimal percentage = difference.divide(last, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        if (percentage.compareTo(BigDecimal.ZERO) > 0) {
            return String.format("You spent %.0f%% more than last month", percentage);
        } else {
            return String.format("You spent %.0f%% less than last month", percentage.abs());
        }
    }
}
