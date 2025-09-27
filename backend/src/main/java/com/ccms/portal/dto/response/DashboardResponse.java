package com.ccms.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private String userName;
    private SummaryResponse summary;
    private List<CardResponse> cards;
    private List<TransactionResponse> transactions;
    private Instant lastUpdated;
}
