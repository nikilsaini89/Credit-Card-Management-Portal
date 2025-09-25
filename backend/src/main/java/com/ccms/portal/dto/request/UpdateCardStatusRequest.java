package com.ccms.portal.dto.request;

import com.ccms.portal.enums.CardStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCardStatusRequest {
    private CardStatus cardStatus;
}
