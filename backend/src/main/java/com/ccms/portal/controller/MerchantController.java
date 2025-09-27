package com.ccms.portal.controller;

import com.ccms.portal.dto.response.MerchantAccountResponse;
import com.ccms.portal.dto.response.MerchantResponse;
import com.ccms.portal.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MerchantController {

  private final MerchantService merchantService;

  @GetMapping
  public ResponseEntity<List<MerchantResponse>> getAllMerchants() {
    log.info("Fetching all merchants");

    try {
      List<MerchantResponse> merchants = merchantService.getAllMerchants();
      return ResponseEntity.ok(merchants);
    } catch (Exception e) {
      log.error("Error fetching merchants: {}", e.getMessage());
      return ResponseEntity.status(500).build();
    }
  }

  @GetMapping("/category/{category}")
  public ResponseEntity<List<MerchantResponse>> getMerchantsByCategory(@PathVariable String category) {
    log.info("Fetching merchants by category: {}", category);

    try {
      List<MerchantResponse> merchants = merchantService.getMerchantsByCategory(category);
      return ResponseEntity.ok(merchants);
    } catch (Exception e) {
      log.error("Error fetching merchants by category: {}", e.getMessage());
      return ResponseEntity.status(500).build();
    }
  }

  @GetMapping("/search")
  public ResponseEntity<List<MerchantResponse>> searchMerchants(@RequestParam String name) {
    log.info("Searching merchants by name: {}", name);

    try {
      List<MerchantResponse> merchants = merchantService.searchMerchants(name);
      return ResponseEntity.ok(merchants);
    } catch (Exception e) {
      log.error("Error searching merchants: {}", e.getMessage());
      return ResponseEntity.status(500).build();
    }
  }

  @GetMapping("/{merchantId}")
  public ResponseEntity<MerchantResponse> getMerchantById(@PathVariable Long merchantId) {
    log.info("Fetching merchant by ID: {}", merchantId);

    try {
      MerchantResponse merchant = merchantService.getMerchantById(merchantId);
      return ResponseEntity.ok(merchant);
    } catch (Exception e) {
      log.error("Error fetching merchant: {}", e.getMessage());
      return ResponseEntity.status(404).build();
    }
  }

  @GetMapping("/accounts")
  public ResponseEntity<List<MerchantAccountResponse>> getMerchantAccounts() {
    log.info("Fetching all merchant accounts");

    try {
      List<MerchantAccountResponse> accounts = merchantService.getMerchantAccounts();
      return ResponseEntity.ok(accounts);
    } catch (Exception e) {
      log.error("Error fetching merchant accounts: {}", e.getMessage());
      return ResponseEntity.status(500).build();
    }
  }

  @GetMapping("/{merchantId}/accounts")
  public ResponseEntity<List<MerchantAccountResponse>> getMerchantAccountsByMerchant(@PathVariable Long merchantId) {
    log.info("Fetching merchant accounts for merchant: {}", merchantId);

    try {
      List<MerchantAccountResponse> accounts = merchantService.getMerchantAccountsByMerchant(merchantId);
      return ResponseEntity.ok(accounts);
    } catch (Exception e) {
      log.error("Error fetching merchant accounts: {}", e.getMessage());
      return ResponseEntity.status(500).build();
    }
  }

  @GetMapping("/accounts/search")
  public ResponseEntity<List<MerchantAccountResponse>> searchMerchantAccounts(@RequestParam String name) {
    log.info("Searching merchant accounts by name: {}", name);

    try {
      List<MerchantAccountResponse> accounts = merchantService.searchMerchantAccounts(name);
      return ResponseEntity.ok(accounts);
    } catch (Exception e) {
      log.error("Error searching merchant accounts: {}", e.getMessage());
      return ResponseEntity.status(500).build();
    }
  }

  @GetMapping("/accounts/{accountId}")
  public ResponseEntity<MerchantAccountResponse> getMerchantAccountById(@PathVariable Long accountId) {
    log.info("Fetching merchant account by ID: {}", accountId);

    try {
      MerchantAccountResponse account = merchantService.getMerchantAccountById(accountId);
      return ResponseEntity.ok(account);
    } catch (Exception e) {
      log.error("Error fetching merchant account: {}", e.getMessage());
      return ResponseEntity.status(404).build();
    }
  }
}
