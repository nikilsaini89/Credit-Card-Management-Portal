package com.ccms.portal.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateCardRequest {
    private Long bankId;
    private Long userId;
    private Double creditLimit;
}
