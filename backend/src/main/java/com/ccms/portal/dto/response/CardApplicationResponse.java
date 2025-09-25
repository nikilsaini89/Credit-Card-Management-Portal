package com.ccms.portal.dto.response;

import com.ccms.portal.entity.CardApplicationEntity;
import com.ccms.portal.enums.CardApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CardApplicationResponse {

    private final Long id;
    private final Long userId;
    private final Long cardTypeId;
    private final Long requestedLimit;
    private final LocalDateTime applicationDate;
    private final CardApplicationStatus status;
    private Long reviewerId;
    private LocalDateTime reviewDate;

    public CardApplicationResponse(CardApplicationEntity entity) {
        this.id = entity.getId();
        this.userId = entity.getUserId();
        this.cardTypeId = entity.getCardTypeId();
        this.requestedLimit = entity.getRequestedLimit();
        this.applicationDate = entity.getApplicationDate();
        this.status = entity.getStatus();
        this.reviewDate = entity.getReviewDate();
        this.reviewerId = entity.getReviewerId();
    }
}
