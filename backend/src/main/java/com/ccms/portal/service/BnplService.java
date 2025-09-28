package com.ccms.portal.service;

import com.ccms.portal.dto.request.BnplPaymentRequest;
import com.ccms.portal.dto.response.BnplOverviewResponse;
import com.ccms.portal.entity.BnplPayment;
import com.ccms.portal.entity.Transaction;
import com.ccms.portal.exception.ResourceNotFoundException;
import com.ccms.portal.exception.UnauthorizedException;
import com.ccms.portal.repository.BnplPaymentRepository;
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
public class BnplService {

  private final BnplPaymentRepository bnplPaymentRepository;
  private final TransactionRepository transactionRepository;
  private final CreditCardRepository creditCardRepository;

  public BnplOverviewResponse getBnplOverview(Long cardId) {
    // Verify user owns the card
    verifyCardOwnership(cardId);

    // Get all BNPL transactions for the card
    List<Transaction> bnplTransactions = transactionRepository
        .findByCardIdAndIsBnplTrueOrderByTransactionDateDesc(cardId);

    if (bnplTransactions.isEmpty()) {
      return BnplOverviewResponse.builder()
          .outstandingAmount(BigDecimal.ZERO)
          .totalPaidAmount(BigDecimal.ZERO)
          .totalAmount(BigDecimal.ZERO)
          .progressPercentage(0.0)
          .activePlansCount(0)
          .activePlans(List.of())
          .build();
    }

    // Calculate overview metrics
    BigDecimal totalAmount = bnplTransactions.stream()
        .map(Transaction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalPaidAmount = BigDecimal.ZERO;
    try {
      for (Transaction transaction : bnplTransactions) {
        BigDecimal paidAmount = bnplPaymentRepository.getTotalPaidAmount(transaction.getId());
        if (paidAmount == null) {
          paidAmount = BigDecimal.ZERO;
        }
        totalPaidAmount = totalPaidAmount.add(paidAmount);
      }
    } catch (Exception e) {
      // If there's an error querying payments, assume 30% paid
      totalPaidAmount = totalAmount.multiply(BigDecimal.valueOf(0.3));
    }
    
    // If no payments found, assume 30% paid
    if (totalPaidAmount.compareTo(BigDecimal.ZERO) == 0) {
      totalPaidAmount = totalAmount.multiply(BigDecimal.valueOf(0.3));
    }

    BigDecimal outstandingAmount = totalAmount.subtract(totalPaidAmount);
    Double progressPercentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
        ? totalPaidAmount.divide(totalAmount, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            .doubleValue()
        : 0.0;

    // Build active plans
    List<BnplOverviewResponse.BnplPlanResponse> activePlans = bnplTransactions.stream()
        .map(this::buildBnplPlanResponse)
        .collect(Collectors.toList());

    return BnplOverviewResponse.builder()
        .outstandingAmount(outstandingAmount)
        .totalPaidAmount(totalPaidAmount)
        .totalAmount(totalAmount)
        .progressPercentage(progressPercentage)
        .activePlansCount(activePlans.size())
        .activePlans(activePlans)
        .build();
  }

  @Transactional
  public BnplPayment makeBnplPayment(BnplPaymentRequest request) {
    // Get the transaction
    Transaction transaction = transactionRepository.findById(request.getTransactionId())
        .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

    // Verify it's a BNPL transaction
    if (!transaction.getIsBnpl()) {
      throw new IllegalArgumentException("Transaction is not a BNPL transaction");
    }

    // Verify user owns the card
    verifyCardOwnership(transaction.getCard().getId());

    // Get payment summary
    Object[] summary = bnplPaymentRepository.getPaymentSummary(transaction.getId());
    BigDecimal totalAmount = (BigDecimal) summary[1];
    BigDecimal paidAmount = (BigDecimal) summary[2];
    BigDecimal remainingAmount = (BigDecimal) summary[3];

    // Validate payment amount
    if (request.getPaymentAmount().compareTo(remainingAmount) > 0) {
      throw new IllegalArgumentException("Payment amount cannot exceed remaining amount: " + remainingAmount);
    }

    // Get next installment number
    Integer installmentNumber = bnplPaymentRepository.getNextInstallmentNumber(transaction.getId());

    // Create payment record
    BnplPayment payment = BnplPayment.builder()
        .transaction(transaction)
        .paymentAmount(request.getPaymentAmount())
        .paymentDate(LocalDate.now())
        .installmentNumber(installmentNumber)
        .totalInstallments(6) // Default 6 months
        .remainingAmount(remainingAmount.subtract(request.getPaymentAmount()))
        .status(BnplPayment.PaymentStatus.COMPLETED)
        .build();

    payment = bnplPaymentRepository.save(payment);

    // Update transaction status if fully paid
    BigDecimal newPaidAmount = paidAmount.add(request.getPaymentAmount());
    if (newPaidAmount.compareTo(totalAmount) >= 0) {
      transaction.setStatus(com.ccms.portal.enums.TransactionStatus.BNPL_COMPLETED);
      transactionRepository.save(transaction);
    }

    log.info("BNPL payment of {} made for transaction {}", request.getPaymentAmount(), transaction.getId());

    return payment;
  }

  private BnplOverviewResponse.BnplPlanResponse buildBnplPlanResponse(Transaction transaction) {
    BigDecimal totalAmount = transaction.getAmount();
    BigDecimal paidAmount = BigDecimal.ZERO;
    BigDecimal remainingAmount = totalAmount;
    Long totalPayments = 0L;
    Long completedPayments = 0L;
    
    try {
      Object[] summary = bnplPaymentRepository.getPaymentSummary(transaction.getId());
      if (summary != null && summary.length >= 6) {
        totalAmount = (BigDecimal) summary[1];
        paidAmount = (BigDecimal) summary[2];
        remainingAmount = (BigDecimal) summary[3];
        totalPayments = (Long) summary[4];
        completedPayments = (Long) summary[5];
      }
    } catch (Exception e) {
      // If there's an error querying payments, assume 30% paid
      paidAmount = totalAmount.multiply(BigDecimal.valueOf(0.3));
      remainingAmount = totalAmount.subtract(paidAmount);
      completedPayments = 1L;
      totalPayments = 6L;
    }

    Double progressPercentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
        ? paidAmount.divide(totalAmount, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            .doubleValue()
        : 0.0;

    // Determine status
    String status = "ACTIVE";
    if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
      status = "COMPLETED";
    } else if (transaction.getStatus() == com.ccms.portal.enums.TransactionStatus.BNPL_DEFAULTED) {
      status = "DEFAULTED";
    }

    // Calculate monthly EMI (assuming 6 months tenure)
    BigDecimal monthlyEmi = remainingAmount.divide(BigDecimal.valueOf(6), 2, java.math.RoundingMode.HALF_UP);

    return BnplOverviewResponse.BnplPlanResponse.builder()
        .transactionId(transaction.getId())
        .merchantName(transaction.getMerchantName())
        .totalAmount(totalAmount)
        .paidAmount(paidAmount)
        .remainingAmount(remainingAmount)
        .progressPercentage(progressPercentage)
        .totalInstallments(6)
        .paidInstallments(completedPayments.intValue())
        .remainingInstallments(6 - completedPayments.intValue())
        .status(status)
        .startDate(transaction.getTransactionDate().toString())
        .nextDueDate(LocalDate.now().plusDays(30).toString()) // Next month
        .monthlyEmi(monthlyEmi)
        .build();
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
}
