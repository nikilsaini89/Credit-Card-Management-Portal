package com.ccms.portal.service;

import com.ccms.portal.dto.response.MerchantAccountResponse;
import com.ccms.portal.dto.response.MerchantResponse;
import com.ccms.portal.entity.Merchant;
import com.ccms.portal.entity.MerchantAccount;
import com.ccms.portal.repository.MerchantAccountRepository;
import com.ccms.portal.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantService {

  private final MerchantRepository merchantRepository;
  private final MerchantAccountRepository merchantAccountRepository;

  public List<MerchantResponse> getAllMerchants() {
    log.info("Fetching all active merchants");

    List<Merchant> merchants = merchantRepository.findByIsActiveTrue();
    return merchants.stream()
        .map(this::mapToMerchantResponse)
        .collect(Collectors.toList());
  }

  public List<MerchantResponse> getMerchantsByCategory(String category) {
    log.info("Fetching merchants by category: {}", category);

    List<Merchant> merchants = merchantRepository.findByCategory(category);
    return merchants.stream()
        .map(this::mapToMerchantResponse)
        .collect(Collectors.toList());
  }

  public List<MerchantResponse> searchMerchants(String name) {
    log.info("Searching merchants by name: {}", name);

    List<Merchant> merchants = merchantRepository.findByNameContainingIgnoreCase(name);
    return merchants.stream()
        .map(this::mapToMerchantResponse)
        .collect(Collectors.toList());
  }

  public MerchantResponse getMerchantById(Long merchantId) {
    log.info("Fetching merchant by ID: {}", merchantId);

    Merchant merchant = merchantRepository.findById(merchantId)
        .orElseThrow(() -> new RuntimeException("Merchant not found"));

    return mapToMerchantResponse(merchant);
  }

  public List<MerchantAccountResponse> getMerchantAccounts() {
    log.info("Fetching all active merchant accounts");

    List<MerchantAccount> accounts = merchantAccountRepository.findByIsActiveTrue();
    return accounts.stream()
        .map(this::mapToMerchantAccountResponse)
        .collect(Collectors.toList());
  }

  public List<MerchantAccountResponse> getMerchantAccountsByMerchant(Long merchantId) {
    log.info("Fetching merchant accounts for merchant: {}", merchantId);

    List<MerchantAccount> accounts = merchantAccountRepository.findByMerchantId(merchantId);
    return accounts.stream()
        .map(this::mapToMerchantAccountResponse)
        .collect(Collectors.toList());
  }

  public List<MerchantAccountResponse> searchMerchantAccounts(String name) {
    log.info("Searching merchant accounts by name: {}", name);

    List<MerchantAccount> accounts = merchantAccountRepository.findByNameContainingIgnoreCase(name);
    return accounts.stream()
        .map(this::mapToMerchantAccountResponse)
        .collect(Collectors.toList());
  }

  public MerchantAccountResponse getMerchantAccountById(Long accountId) {
    log.info("Fetching merchant account by ID: {}", accountId);

    MerchantAccount account = merchantAccountRepository.findById(accountId)
        .orElseThrow(() -> new RuntimeException("Merchant account not found"));

    return mapToMerchantAccountResponse(account);
  }

  private MerchantResponse mapToMerchantResponse(Merchant merchant) {
    return MerchantResponse.builder()
        .id(merchant.getId())
        .name(merchant.getName())
        .category(merchant.getCategory())
        .description(merchant.getDescription())
        .logoUrl(merchant.getLogoUrl())
        .isActive(merchant.getIsActive())
        .build();
  }

  private MerchantAccountResponse mapToMerchantAccountResponse(MerchantAccount account) {
    return MerchantAccountResponse.builder()
        .id(account.getId())
        .merchantId(account.getMerchant().getId())
        .name(account.getName())
        .accountNumber(account.getAccountNumber())
        .bankName(account.getBankName())
        .isActive(account.getIsActive())
        .build();
  }
}
