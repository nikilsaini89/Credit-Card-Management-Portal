package com.ccms.portal.service;

import com.ccms.portal.dto.request.PaymentRequest;
import com.ccms.portal.dto.response.StatementResponse;
import com.ccms.portal.entity.CreditCardStatement;
import com.ccms.portal.entity.Transaction;
import com.ccms.portal.exception.ResourceNotFoundException;
import com.ccms.portal.exception.UnauthorizedException;
import com.ccms.portal.repository.CreditCardStatementRepository;
import com.ccms.portal.repository.TransactionRepository;
import com.ccms.portal.repository.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditCardStatementService {

  private final CreditCardStatementRepository statementRepository;
  private final TransactionRepository transactionRepository;
  private final CreditCardRepository creditCardRepository;

  public StatementResponse getCurrentStatement(Long cardId) {
    // Verify user owns the card
    verifyCardOwnership(cardId);

    // Get or create current statement
    CreditCardStatement statement = statementRepository.findFirstByCardIdOrderByStatementDateDesc(cardId)
        .orElseGet(() -> createCurrentStatement(cardId));

    return mapToStatementResponse(statement);
  }

  @Transactional
  public CreditCardStatement createCurrentStatement(Long cardId) {
    // Verify user owns the card
    verifyCardOwnership(cardId);

    // Check if statement already exists for current month
    LocalDate today = LocalDate.now();
    LocalDate firstDayOfMonth = today.withDayOfMonth(1);

    if (statementRepository.existsByCardIdAndStatementDate(cardId, today)) {
      return statementRepository.findFirstByCardIdOrderByStatementDateDesc(cardId)
          .orElseThrow(() -> new ResourceNotFoundException("Statement not found"));
    }

    // Calculate statement amount from current month transactions
    List<Transaction> monthlyTransactions = transactionRepository.findByCardIdAndDateRange(
        cardId, firstDayOfMonth, today);

    BigDecimal statementAmount = monthlyTransactions.stream()
        .map(Transaction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Calculate due date (15 days from today)
    LocalDate dueDate = today.plusDays(15);

    // Create statement
    CreditCardStatement statement = CreditCardStatement.builder()
        .cardId(cardId)
        .statementDate(today)
        .dueDate(dueDate)
        .statementAmount(statementAmount)
        .paidAmount(BigDecimal.ZERO)
        .status("PENDING")
        .build();

    return statementRepository.save(statement);
  }

  @Transactional
  public StatementResponse makePayment(PaymentRequest paymentRequest) {
    CreditCardStatement statement = statementRepository.findById(paymentRequest.getStatementId())
        .orElseThrow(() -> new ResourceNotFoundException("Statement not found"));

    // Verify user owns the card
    verifyCardOwnership(statement.getCardId());

    // Validate payment amount
    if (paymentRequest.getPaymentAmount().compareTo(statement.getAmountDue()) > 0) {
      throw new IllegalArgumentException("Payment amount cannot exceed amount due");
    }

    // Update paid amount
    BigDecimal newPaidAmount = statement.getPaidAmount().add(paymentRequest.getPaymentAmount());
    statement.setPaidAmount(newPaidAmount);

    // Update status
    if (newPaidAmount.compareTo(statement.getStatementAmount()) >= 0) {
      statement.setStatus("PAID");
    } else if (LocalDate.now().isAfter(statement.getDueDate())) {
      statement.setStatus("OVERDUE");
    } else {
      statement.setStatus("PARTIAL");
    }

    statement = statementRepository.save(statement);

    log.info("Payment of {} made for statement {}", paymentRequest.getPaymentAmount(), statement.getId());

    return mapToStatementResponse(statement);
  }

  public List<StatementResponse> getStatementHistory(Long cardId) {
    // Verify user owns the card
    verifyCardOwnership(cardId);

    List<CreditCardStatement> statements = statementRepository.findByCardIdOrderByStatementDateDesc(cardId);
    return statements.stream()
        .map(this::mapToStatementResponse)
        .collect(Collectors.toList());
  }

  private void verifyCardOwnership(Long cardId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof com.ccms.portal.util.JwtUserDetails)) {
      throw new UnauthorizedException("User not authenticated");
    }

    com.ccms.portal.util.JwtUserDetails userDetails = (com.ccms.portal.util.JwtUserDetails) authentication
        .getPrincipal();
    Long userId = userDetails.getUserId();

    // Verify card belongs to user
    boolean cardExists = creditCardRepository.existsByIdAndUserId(cardId, userId);
    if (!cardExists) {
      throw new UnauthorizedException("Card does not belong to user");
    }
  }

  private StatementResponse mapToStatementResponse(CreditCardStatement statement) {
    return StatementResponse.builder()
        .id(statement.getId())
        .cardId(statement.getCardId())
        .statementDate(statement.getStatementDate())
        .dueDate(statement.getDueDate())
        .statementAmount(statement.getStatementAmount())
        .paidAmount(statement.getPaidAmount())
        .amountDue(statement.getAmountDue())
        .status(statement.getStatus())
        .isPaid(statement.isPaid())
        .isOverdue(statement.isOverdue())
        .build();
  }
}
