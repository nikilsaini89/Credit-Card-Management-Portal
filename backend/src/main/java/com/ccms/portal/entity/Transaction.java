package com.ccms.portal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "serial_no")
  private Long serialNo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "card_id", nullable = false)
  private CreditCardEntity card;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "merchant_account_id")
  private MerchantAccount merchantAccount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "processor_id")
  private PaymentProcessor processor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "acquirer_bank_id")
  private Bank acquirerBank;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "issuer_bank_id")
  private Bank issuerBank;

  @Column(name = "network", length = 30)
  private String network; 

  @Column(name = "amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private TransactionStatus status = TransactionStatus.INITIATED;

  @Column(name = "is_bnpl")
  private Boolean isBnpl = false;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  public enum TransactionStatus {
    INITIATED, AUTHORIZED, CAPTURED, SETTLED, DECLINED, REFUNDED
  }
}
