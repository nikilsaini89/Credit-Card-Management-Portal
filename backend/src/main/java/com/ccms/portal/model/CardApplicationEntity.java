package com.ccms.portal.model;

import com.ccms.portal.enums.CardApplicationStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Data
@Entity
@Table(name="credit_card_application")
public class CardApplicationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long cardTypeId;

    private Long requestedLimit;

    @CreationTimestamp
    private LocalDateTime applicationDate;

    @Enumerated(EnumType.STRING)
    private CardApplicationStatus status;

    private Long reviewerId;
    private LocalDateTime reviewDate;
}
