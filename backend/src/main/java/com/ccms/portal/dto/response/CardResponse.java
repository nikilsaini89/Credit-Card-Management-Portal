package com.ccms.portal.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
public class CardResponse {
    private String id;
    private String name;
    private String number;
    private Double totalLimit;
    private Double availableLimit;
    private String status;
}
