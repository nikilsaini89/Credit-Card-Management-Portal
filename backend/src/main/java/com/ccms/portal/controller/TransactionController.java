package com.ccms.portal.controller;

import com.ccms.portal.dto.request.CreateTransactionRequest;
import com.ccms.portal.dto.response.AnalyticsResponse;
import com.ccms.portal.dto.response.TransactionHistoryResponse;
import com.ccms.portal.dto.response.TransactionResponse;
import com.ccms.portal.service.TransactionService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TransactionController {

  private final TransactionService transactionService;

  @PostMapping
  public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
    log.info("Creating new transaction for card: {}", request.getCardId());
    try {
      TransactionResponse response = transactionService.createTransaction(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (Exception e) {
      log.error("Error creating transaction: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @GetMapping("/{userId}")
  public ResponseEntity<TransactionHistoryResponse> getTransactionHistory(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) Boolean isBnpl,
      @RequestParam(required = false) String merchant) {

    log.info(
        "Fetching transaction history for user: {} with filters - status: {}, category: {}, isBnpl: {}, merchant: {}",
        userId, status, category, isBnpl, merchant);

    try {
      TransactionHistoryResponse response = transactionService.getTransactionHistory(
          userId, page, size, status, category, isBnpl, merchant);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error fetching transaction history: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/card/{cardId}")
  public ResponseEntity<List<TransactionResponse>> getTransactionsByCard(@PathVariable Long cardId) {
    log.info("Fetching transactions for card: {}", cardId);

    try {
      List<TransactionResponse> transactions = transactionService.getTransactionsByCard(cardId);
      return ResponseEntity.ok(transactions);
    } catch (Exception e) {
      log.error("Error fetching transactions for card: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/{transactionId}/details")
  public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long transactionId) {
    log.info("Fetching transaction details for ID: {}", transactionId);

    try {
      TransactionResponse transaction = transactionService.getTransactionById(transactionId);
      return ResponseEntity.ok(transaction);
    } catch (Exception e) {
      log.error("Error fetching transaction details: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @GetMapping("/{userId}/analytics")
  public ResponseEntity<AnalyticsResponse> getAnalytics(@PathVariable Long userId) {
    log.info("Fetching analytics for user: {}", userId);

    try {
      AnalyticsResponse analytics = transactionService.getAnalytics(userId);
      return ResponseEntity.ok(analytics);
    } catch (Exception e) {
      log.error("Error fetching analytics: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @DeleteMapping("/{transactionId}")
  public ResponseEntity<Void> deleteTransaction(@PathVariable Long transactionId) {
    log.info("Deleting transaction: {}", transactionId);

    try {
      transactionService.deleteTransaction(transactionId);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Error deleting transaction: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @GetMapping("/{userId}/export/csv")
  public void exportTransactionsCSV(@PathVariable Long userId, HttpServletResponse response) {
    log.info("Exporting transactions as CSV for user: {}", userId);

    try {
      response.setContentType("text/csv");
      response.setHeader("Content-Disposition", "attachment; filename=transactions.csv");

      // Get all transactions for the user (no pagination for export)
      TransactionHistoryResponse historyResponse = transactionService.getTransactionHistory(
          userId, 0, Integer.MAX_VALUE, null, null, null, null);

      PrintWriter writer = response.getWriter();

      // Write CSV header
      writer.println("Transaction ID,Date,Merchant,Amount,Category,Status,Card,Type");

      // Write transaction data
      for (TransactionResponse transaction : historyResponse.getTransactions()) {
        writer.printf("%s,%s,%s,%.2f,%s,%s,%s %s,%s%n",
            transaction.getTransactionId(),
            transaction.getCreatedAt().toString(),
            transaction.getMerchant(),
            transaction.getAmount(),
            transaction.getCategory(),
            transaction.getStatus(),
            transaction.getCardType(),
            transaction.getLastFour(),
            transaction.getIsBnpl() ? "BNPL" : "Regular");
      }

      writer.flush();
      log.info("CSV export completed successfully");

    } catch (IOException e) {
      log.error("Error exporting CSV: {}", e.getMessage());
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      log.error("Error preparing CSV export: {}", e.getMessage());
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/{userId}/export/pdf")
  public void exportTransactionsPDF(@PathVariable Long userId, HttpServletResponse response) {
    log.info("Exporting transactions as PDF for user: {}", userId);

    try {
      response.setContentType("application/pdf");
      response.setHeader("Content-Disposition", "attachment; filename=transactions.pdf");

      // For now, redirect to CSV export
      // TODO: Implement PDF generation using a library like iText or Apache PDFBox
      exportTransactionsCSV(userId, response);

    } catch (Exception e) {
      log.error("Error exporting PDF: {}", e.getMessage());
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
