package com.ccms.portal.config;

import com.ccms.portal.entity.*;
import com.ccms.portal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// @Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

  private final MerchantRepository merchantRepository;
  private final MerchantAccountRepository merchantAccountRepository;
  private final PaymentProcessorRepository paymentProcessorRepository;

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    log.info("Starting data seeding...");

    // Create merchants
    Merchant merchant1 = createMerchant("Amazon", "Shopping", "Online marketplace", "https://amazon.com/logo.png");
    Merchant merchant2 = createMerchant("McDonald's", "Food", "Fast food restaurant", "https://mcdonalds.com/logo.png");
    Merchant merchant3 = createMerchant("Uber", "Travel", "Ride sharing service", "https://uber.com/logo.png");

    // Create merchant accounts
    MerchantAccount account1 = createMerchantAccount(merchant1, "Amazon India", "ACC001", "HDFC Bank");
    MerchantAccount account2 = createMerchantAccount(merchant2, "McDonald's India", "ACC002", "SBI");
    MerchantAccount account3 = createMerchantAccount(merchant3, "Uber India", "ACC003", "HDFC Bank");

    // Create payment processors
    PaymentProcessor processor1 = createPaymentProcessor("Razorpay", "https://api.razorpay.com");
    PaymentProcessor processor2 = createPaymentProcessor("PayU", "https://api.payu.com");

    log.info("Data seeding completed successfully!");
  }

  private Merchant createMerchant(String name, String category, String description, String logoUrl) {
    Merchant merchant = new Merchant();
    merchant.setName(name);
    merchant.setCategory(category);
    merchant.setDescription(description);
    merchant.setLogoUrl(logoUrl);
    merchant.setIsActive(true);
    merchant.setCreatedAt(LocalDateTime.now());
    return merchantRepository.save(merchant);
  }

  private MerchantAccount createMerchantAccount(Merchant merchant, String name, String accountNumber, String bankName) {
    MerchantAccount account = new MerchantAccount();
    account.setMerchant(merchant);
    account.setName(name);
    account.setAccountNumber(accountNumber);
    account.setBankName(bankName);
    account.setCurrency("INR");
    account.setIsActive(true);
    account.setCreatedAt(LocalDateTime.now());
    return merchantAccountRepository.save(account);
  }

  private PaymentProcessor createPaymentProcessor(String name, String apiEndpoint) {
    PaymentProcessor processor = new PaymentProcessor();
    processor.setName(name);
    processor.setApiEndpoint(apiEndpoint);
    processor.setCreatedAt(LocalDateTime.now());
    return paymentProcessorRepository.save(processor);
  }
}
