package com.ccms.portal.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardApplicationRequest {
    @NotNull(message = "UserId is required")
    private Long userId;
    @NotNull(message = "cardTypeId is required")
    private Long cardTypeId;

    @NotNull(message="requestLimit is required")
    private Long requestLimit;

}
