package com.ccms.portal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bnpl_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BnplPlan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "transaction_id", nullable = false)
  private Long transactionId;

  @Column(name = "merchant", length = 100)
  private String merchant;

  @Column(name = "category", length = 50)
  private String category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "transaction_id", nullable = false, insertable = false, updatable = false)
  private Transaction transaction;

  @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "paid_amount", precision = 12, scale = 2)
  private BigDecimal paidAmount = BigDecimal.ZERO;

  @Column(name = "remaining_amount", precision = 12, scale = 2)
  private BigDecimal remainingAmount;

  @Column(name = "tenure_months", nullable = false)
  private Integer tenureMonths;

  @Column(name = "paid_months")
  private Integer paidMonths = 0;

  @Column(name = "remaining_months")
  private Integer remainingMonths;

  @Column(name = "monthly_emi", nullable = false, precision = 12, scale = 2)
  private BigDecimal monthlyEmi;

  @Column(name = "next_due_date")
  private LocalDate nextDueDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private BnplStatus status = BnplStatus.ACTIVE;

  @Column(name = "is_active")
  private Boolean isActive = true;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    if (remainingAmount == null) {
      remainingAmount = totalAmount;
    }
    if (remainingMonths == null) {
      remainingMonths = tenureMonths;
    }
  }

  public enum BnplStatus {
    ACTIVE, COMPLETED, DEFAULTED, CANCELLED
  }
}
