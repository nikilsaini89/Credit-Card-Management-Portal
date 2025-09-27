package com.ccms.portal.dto.response;

import lombok.*;

import java.time.Instant;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String id;
    private String cardId;
    private Double amount;
    private String merchant;
    private String category;
    private String mode;
    private Instant date;
}
