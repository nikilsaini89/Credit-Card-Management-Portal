package com.ccms.portal.entity;

import com.ccms.portal.enums.NetworkType;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "card_type")
@Getter
public class CardTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NetworkType networkType;

    private Double minCardLimit;

    private Double maxCardLimit;

    private String description;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "cardType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CreditCardEntity> creditCards = new ArrayList<>();
}
