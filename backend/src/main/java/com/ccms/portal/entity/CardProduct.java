package com.ccms.portal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "card_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class CardProduct {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "network_type", nullable = false, length = 30)
  private String networkType; // VISA, MASTERCARD, AMEX, RUPAY

  @Column(name = "description", length = 500)
  private String description;

  @Column(name = "min_limit", precision = 12, scale = 2)
  private BigDecimal minLimit;

  @Column(name = "max_limit", precision = 12, scale = 2)
  private BigDecimal maxLimit;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
