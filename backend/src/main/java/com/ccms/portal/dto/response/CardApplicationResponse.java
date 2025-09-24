package com.ccms.portal.dto.response;

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
}
