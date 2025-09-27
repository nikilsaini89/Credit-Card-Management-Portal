package com.ccms.portal.service;

import com.ccms.portal.dto.request.CreateTransactionRequest;
import com.ccms.portal.dto.response.AnalyticsResponse;
import com.ccms.portal.dto.response.TransactionHistoryResponse;
import com.ccms.portal.dto.response.TransactionResponse;
import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.entity.MerchantAccount;
import com.ccms.portal.entity.Transaction;
import com.ccms.portal.entity.Transaction.TransactionStatus;
import com.ccms.portal.repository.CreditCardRepository;
import com.ccms.portal.repository.MerchantAccountRepository;
import com.ccms.portal.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final CreditCardRepository creditCardRepository;
  private final MerchantAccountRepository merchantAccountRepository;

  @Transactional
  public TransactionResponse createTransaction(CreateTransactionRequest request) {
    log.info("Creating transaction for card: {}", request.getCardId());

    // Validate card exists
    CreditCardEntity card = creditCardRepository.findById(request.getCardId())
        .orElseThrow(() -> new RuntimeException("Card not found"));

    // Validate merchant account exists
    MerchantAccount merchantAccount = merchantAccountRepository.findById(request.getMerchantAccountId())
        .orElseThrow(() -> new RuntimeException("Merchant account not found"));

    // Create transaction
    Transaction transaction = new Transaction();
    transaction.setCard(card);
    transaction.setMerchantAccount(merchantAccount);
    transaction.setAmount(request.getAmount());
    transaction.setMerchant(request.getMerchant());
    transaction.setCategory(request.getCategory());
    transaction.setStatus(request.getStatus());
    transaction.setIsBnpl(request.getIsBnpl());
    transaction.setCardType(request.getCardType());
    transaction.setLastFour(request.getLastFour());
    transaction.setNetwork(request.getNetwork());
    transaction.setCreatedAt(LocalDateTime.now());

    // Generate serial number
    Long maxSerialNo = transactionRepository.findMaxSerialNo();
    transaction.setSerialNo(maxSerialNo != null ? maxSerialNo + 1 : 1L);

    Transaction savedTransaction = transactionRepository.save(transaction);
    log.info("Transaction created successfully with ID: {}", savedTransaction.getId());

    return mapToTransactionResponse(savedTransaction);
  }

  public TransactionHistoryResponse getTransactionHistory(Long userId, int page, int size,
      String status, String category,
      Boolean isBnpl, String merchant) {
    log.info("Fetching transaction history for user: {}", userId);

    Pageable pageable = PageRequest.of(page, size);
    Page<Transaction> transactionPage;

    if (status != null || category != null || isBnpl != null || merchant != null) {
      // Use filtered query
      TransactionStatus transactionStatus = status != null ? TransactionStatus.valueOf(status.toUpperCase()) : null;
      List<Transaction> transactions = transactionRepository.findByUserIdWithFilters(
          userId, transactionStatus, category, isBnpl, merchant);

      // Convert to page manually
      int start = (int) pageable.getOffset();
      int end = Math.min((start + pageable.getPageSize()), transactions.size());
      List<Transaction> pageContent = transactions.subList(start, end);

      transactionPage = new org.springframework.data.domain.PageImpl<>(
          pageContent, pageable, transactions.size());
    } else {
      transactionPage = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    List<TransactionResponse> transactionResponses = transactionPage.getContent()
        .stream()
        .map(this::mapToTransactionResponse)
        .collect(Collectors.toList());

    // Calculate summary statistics
    BigDecimal totalAmount = transactionRepository.getTotalAmountByUserId(userId);
    BigDecimal totalSpent = transactionRepository.getTotalAmountByUserIdAndStatus(userId, TransactionStatus.COMPLETED);
    BigDecimal averageTransaction = transactionRepository.getAverageTransactionAmountByUserId(userId);
    Long totalTransactions = transactionRepository.getTransactionCountByUserId(userId);

    return TransactionHistoryResponse.builder()
        .transactions(transactionResponses)
        .totalTransactions(totalTransactions)
        .totalAmount(totalAmount)
        .totalSpent(totalSpent)
        .averageTransaction(averageTransaction)
        .bnplPlansCount(0) // TODO: Implement BNPL count
        .currentPage(transactionPage.getNumber())
        .totalPages(transactionPage.getTotalPages())
        .hasNext(transactionPage.hasNext())
        .hasPrevious(transactionPage.hasPrevious())
        .build();
  }

  public AnalyticsResponse getAnalytics(Long userId) {
    log.info("Fetching analytics for user: {}", userId);

    // Get category breakdown
    List<Object[]> categoryData = transactionRepository.getCategoryBreakdownByUserId(userId);
    List<AnalyticsResponse.CategoryBreakdown> categoryBreakdown = categoryData.stream()
        .map(this::mapToCategoryBreakdown)
        .collect(Collectors.toList());

    // Get top merchants
    List<Object[]> merchantData = transactionRepository.getTopMerchantsByUserId(userId);
    List<AnalyticsResponse.TopMerchant> topMerchants = merchantData.stream()
        .map(this::mapToTopMerchant)
        .collect(Collectors.toList());

    // Calculate monthly spending
    List<Object[]> monthlyData = transactionRepository.getMonthlySpendingByUserId(userId);
    BigDecimal thisMonth = calculateThisMonthSpending(monthlyData);
    BigDecimal lastMonth = calculateLastMonthSpending(monthlyData);
    double change = calculateChangePercentage(thisMonth, lastMonth);

    return AnalyticsResponse.builder()
        .thisMonth(thisMonth)
        .lastMonth(lastMonth)
        .change(change)
        .categoryBreakdown(categoryBreakdown)
        .topMerchants(topMerchants)
        .build();
  }

  public List<TransactionResponse> getTransactionsByCard(Long cardId) {
    log.info("Fetching transactions for card: {}", cardId);

    List<Transaction> transactions = transactionRepository.findByCardIdOrderByCreatedAtDesc(cardId);
    return transactions.stream()
        .map(this::mapToTransactionResponse)
        .collect(Collectors.toList());
  }

  public TransactionResponse getTransactionById(Long transactionId) {
    log.info("Fetching transaction by ID: {}", transactionId);

    Transaction transaction = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new RuntimeException("Transaction not found"));

    return mapToTransactionResponse(transaction);
  }

  @Transactional
  public void deleteTransaction(Long transactionId) {
    log.info("Deleting transaction: {}", transactionId);

    if (!transactionRepository.existsById(transactionId)) {
      throw new RuntimeException("Transaction not found");
    }

    transactionRepository.deleteById(transactionId);
    log.info("Transaction deleted successfully");
  }

  private TransactionResponse mapToTransactionResponse(Transaction transaction) {
    return TransactionResponse.builder()
        .id(transaction.getId())
        .serialNo(transaction.getSerialNo())
        .cardId(transaction.getCard().getId())
        .cardType(transaction.getCardType())
        .lastFour(transaction.getLastFour())
        .merchantAccountId(transaction.getMerchantAccount() != null ? transaction.getMerchantAccount().getId() : null)
        .merchant(transaction.getMerchant())
        .category(transaction.getCategory())
        .amount(transaction.getAmount())
        .status(transaction.getStatus())
        .isBnpl(transaction.getIsBnpl())
        .network(transaction.getNetwork())
        .createdAt(transaction.getCreatedAt())
        .transactionId(transaction.getSerialNo() != null ? String.format("TXN%06d", transaction.getSerialNo()) : null)
        .build();
  }

  private AnalyticsResponse.CategoryBreakdown mapToCategoryBreakdown(Object[] data) {
    String category = (String) data[0];
    Long count = (Long) data[1];
    BigDecimal amount = (BigDecimal) data[2];

    // Calculate percentage (simplified - you might want to calculate based on
    // total)
    double percentage = count.doubleValue() * 10; // Placeholder calculation

    return AnalyticsResponse.CategoryBreakdown.builder()
        .name(category)
        .percentage(percentage)
        .amount(amount)
        .color(getCategoryColor(category))
        .build();
  }

  private AnalyticsResponse.TopMerchant mapToTopMerchant(Object[] data) {
    String merchant = (String) data[0];
    Long count = (Long) data[1];
    BigDecimal amount = (BigDecimal) data[2];

    return AnalyticsResponse.TopMerchant.builder()
        .name(merchant)
        .count(count.intValue())
        .amount(amount)
        .build();
  }

  private String getCategoryColor(String category) {
    return switch (category.toLowerCase()) {
      case "shopping" -> "#ffd60a";
      case "food" -> "#f97316";
      case "travel" -> "#0b2540";
      case "entertainment" -> "#111827";
      default -> "#6b7280";
    };
  }

  private BigDecimal calculateThisMonthSpending(List<Object[]> monthlyData) {
    LocalDateTime now = LocalDateTime.now();
    return monthlyData.stream()
        .filter(data -> {
          Integer year = (Integer) data[0];
          Integer month = (Integer) data[1];
          return year.equals(now.getYear()) && month.equals(now.getMonthValue());
        })
        .map(data -> (BigDecimal) data[2])
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal calculateLastMonthSpending(List<Object[]> monthlyData) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime lastMonth = now.minusMonths(1);
    return monthlyData.stream()
        .filter(data -> {
          Integer year = (Integer) data[0];
          Integer month = (Integer) data[1];
          return year.equals(lastMonth.getYear()) && month.equals(lastMonth.getMonthValue());
        })
        .map(data -> (BigDecimal) data[2])
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private double calculateChangePercentage(BigDecimal thisMonth, BigDecimal lastMonth) {
    if (lastMonth.compareTo(BigDecimal.ZERO) == 0) {
      return thisMonth.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
    }
    return thisMonth.subtract(lastMonth)
        .divide(lastMonth, 4, java.math.RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .doubleValue();
  }
}
