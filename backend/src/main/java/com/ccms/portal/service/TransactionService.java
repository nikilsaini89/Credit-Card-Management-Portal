package com.ccms.portal.service;

import com.ccms.portal.dto.request.CreateTransactionRequest;
import com.ccms.portal.dto.response.AnalyticsResponse;
import com.ccms.portal.dto.response.TransactionHistoryResponse;
import com.ccms.portal.dto.response.TransactionResponse;
import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.entity.Transaction;
import com.ccms.portal.enums.TransactionStatus;
import com.ccms.portal.exception.CreditCardNotFoundException;
import com.ccms.portal.exception.InsufficientLimitException;
import com.ccms.portal.exception.InvalidTransactionException;
import com.ccms.portal.exception.TransactionNotFoundException;
import com.ccms.portal.repository.CreditCardRepository;
import com.ccms.portal.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

        private final TransactionRepository transactionRepository;
        private final CreditCardRepository creditCardRepository;

        /**
         * Create a new transaction
         */
        @Transactional
        public TransactionResponse createTransaction(CreateTransactionRequest request) {
                log.info("Creating transaction for card: {}", request.getCardId());

                // Validate card exists
                CreditCardEntity card = creditCardRepository.findById(request.getCardId())
                                .orElseThrow(() -> new CreditCardNotFoundException(
                                                "Card not found with ID: " + request.getCardId()));

                // Check if sufficient credit limit
                BigDecimal availableLimit = BigDecimal.valueOf(card.getAvailableLimit());
                if (availableLimit.compareTo(request.getAmount()) < 0) {
                        throw new InsufficientLimitException("Insufficient credit limit. Available: " +
                                        card.getAvailableLimit() + ", Required: " + request.getAmount());
                }

                // Validate transaction amount
                if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new InvalidTransactionException("Transaction amount must be greater than 0");
                }

                // Create transaction
                Transaction transaction = new Transaction();
                transaction.setCard(card);
                transaction.setMerchantName(request.getMerchantName());
                transaction.setAmount(request.getAmount());
                transaction.setTransactionDate(request.getTransactionDate());
                transaction.setCategory(request.getCategory());
                transaction.setIsBnpl(request.getIsBnpl());
                transaction.setCardType(request.getCardType());
                transaction.setLastFour(request.getLastFour());
                transaction.setStatus(TransactionStatus.COMPLETED);

                Transaction savedTransaction = transactionRepository.save(transaction);
                log.info("Transaction created successfully with ID: {}", savedTransaction.getId());

                // Update available credit limit
                BigDecimal newAvailableLimit = availableLimit.subtract(request.getAmount());
                card.setAvailableLimit(newAvailableLimit.doubleValue());
                creditCardRepository.save(card);
                log.info("Updated available limit for card {} to: {}", card.getId(), newAvailableLimit);

                return mapToTransactionResponse(savedTransaction);
        }

        /**
         * Get transaction history for a card
         */
        public TransactionHistoryResponse getTransactionHistory(Long cardId, int page, int size,
                        String category, Boolean isBnpl, String merchantName) {
                log.info("Fetching transaction history for card: {}, page: {}, size: {}", cardId, page, size);

                // Validate card exists
                if (!creditCardRepository.existsById(cardId)) {
                        throw new CreditCardNotFoundException("Card not found with ID: " + cardId);
                }

                Pageable pageable = PageRequest.of(page, size);
                Page<Transaction> transactionPage;

                if (category != null || isBnpl != null || merchantName != null) {
                        transactionPage = transactionRepository.findByCardIdWithFilters(
                                        cardId, category, isBnpl, merchantName, pageable);
                } else {
                        transactionPage = transactionRepository.findByCardIdOrderByTransactionDateDesc(cardId,
                                        pageable);
                }

                List<TransactionResponse> transactions = transactionPage.getContent()
                                .stream()
                                .map(this::mapToTransactionResponse)
                                .collect(Collectors.toList());

                return TransactionHistoryResponse.builder()
                                .transactions(transactions)
                                .currentPage(transactionPage.getNumber())
                                .totalPages(transactionPage.getTotalPages())
                                .totalElements(transactionPage.getTotalElements())
                                .pageSize(transactionPage.getSize())
                                .hasNext(transactionPage.hasNext())
                                .hasPrevious(transactionPage.hasPrevious())
                                .build();
        }

        /**
         * Get spending analytics for a card
         */
        public AnalyticsResponse getSpendingAnalytics(Long cardId) {
                log.info("Fetching spending analytics for card: {}", cardId);

                // Validate card exists
                if (!creditCardRepository.existsById(cardId)) {
                        throw new CreditCardNotFoundException("Card not found with ID: " + cardId);
                }

                // Get total spent
                Double totalSpent = transactionRepository.getTotalSpentByCardId(cardId);
                Long transactionCount = transactionRepository.countByCardId(cardId);
                Long bnplCount = (long) transactionRepository
                                .findByCardIdAndIsBnplTrueOrderByTransactionDateDesc(cardId)
                                .size();

                // Get category breakdown
                List<Object[]> categoryData = transactionRepository.getSpendingByCategory(cardId);
                List<AnalyticsResponse.CategorySpending> categoryBreakdown = categoryData.stream()
                                .map(data -> {
                                        String category = (String) data[0];
                                        BigDecimal amount = (BigDecimal) data[1];
                                        Double percentage = totalSpent > 0 ? (amount.doubleValue() / totalSpent) * 100
                                                        : 0.0;

                                        return AnalyticsResponse.CategorySpending.builder()
                                                        .category(category)
                                                        .amount(amount)
                                                        .percentage(percentage)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                // Get monthly trends (last 24 months to include sample data)
                LocalDate startDate = LocalDate.now().minusMonths(24);
                List<Object[]> monthlyData = transactionRepository.getMonthlySpendingTrend(cardId, startDate);
                List<AnalyticsResponse.MonthlySpending> monthlyTrends = monthlyData.stream()
                                .map(data -> {
                                        Integer year = (Integer) data[0];
                                        Integer month = (Integer) data[1];
                                        BigDecimal amount = (BigDecimal) data[2];
                                        String monthName = LocalDate.of(year, month, 1)
                                                        .getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

                                        return AnalyticsResponse.MonthlySpending.builder()
                                                        .year(year)
                                                        .month(month)
                                                        .amount(amount)
                                                        .monthName(monthName)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                // Calculate average transaction amount
                BigDecimal averageAmount = transactionCount > 0
                                ? BigDecimal.valueOf(totalSpent).divide(BigDecimal.valueOf(transactionCount), 2,
                                                BigDecimal.ROUND_HALF_UP)
                                : BigDecimal.ZERO;

                return AnalyticsResponse.builder()
                                .totalSpent(BigDecimal.valueOf(totalSpent))
                                .transactionCount(transactionCount)
                                .categoryBreakdown(categoryBreakdown)
                                .monthlyTrends(monthlyTrends)
                                .averageTransactionAmount(averageAmount)
                                .bnplTransactionCount(bnplCount)
                                .build();
        }

        /**
         * Get total amount spent on a card
         */
        public Double getTotalSpentByCardId(Long cardId) {
                log.info("Fetching total spent for card: {}", cardId);
                return transactionRepository.getTotalSpentByCardId(cardId);
        }

        /**
         * Get transaction count for a card
         */
        public Long getTransactionCountByCardId(Long cardId) {
                log.info("Fetching transaction count for card: {}", cardId);
                return transactionRepository.countByCardId(cardId);
        }

        /**
         * Get BNPL transactions for a card
         */
        public List<TransactionResponse> getBnplTransactions(Long cardId) {
                log.info("Fetching BNPL transactions for card: {}", cardId);
                return transactionRepository.findByCardIdAndIsBnplTrueOrderByTransactionDateDesc(cardId)
                                .stream()
                                .map(this::mapToTransactionResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Get transaction by ID
         */
        public TransactionResponse getTransactionById(Long transactionId) {
                log.info("Fetching transaction by ID: {}", transactionId);
                Transaction transaction = transactionRepository.findById(transactionId)
                                .orElseThrow(() -> new TransactionNotFoundException(
                                                "Transaction not found with ID: " + transactionId));
                return mapToTransactionResponse(transaction);
        }

        /**
         * Map Transaction entity to TransactionResponse DTO
         */
        private TransactionResponse mapToTransactionResponse(Transaction transaction) {
                return TransactionResponse.builder()
                                .id(transaction.getId())
                                .cardId(transaction.getCard().getId())
                                .merchantName(transaction.getMerchantName())
                                .amount(transaction.getAmount())
                                .transactionDate(transaction.getTransactionDate())
                                .category(transaction.getCategory())
                                .isBnpl(transaction.getIsBnpl())
                                .cardType(transaction.getCardType())
                                .lastFour(transaction.getLastFour())
                                .status(transaction.getStatus())
                                .createdAt(transaction.getCreatedAt())
                                .build();
        }

        /**
         * Get monthly spending trends for a card with custom start date
         */
        public List<Object[]> getMonthlySpendingTrends(Long cardId, LocalDate startDate) {
                log.info("Fetching monthly spending trends for card: {}, startDate: {}", cardId, startDate);

                // Validate card exists
                if (!creditCardRepository.existsById(cardId)) {
                        throw new CreditCardNotFoundException("Card not found with ID: " + cardId);
                }

                List<Object[]> monthlyData = transactionRepository.getMonthlySpendingTrend(cardId, startDate);
                return monthlyData.stream()
                                .map(data -> new Object[] { data[0], data[1], data[2] })
                                .collect(Collectors.toList());
        }
}
