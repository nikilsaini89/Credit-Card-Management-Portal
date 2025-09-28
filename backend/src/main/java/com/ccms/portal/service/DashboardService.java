package com.ccms.portal.service;

import com.ccms.portal.dto.response.DashboardResponse;
import com.ccms.portal.dto.response.SummaryResponse;
import com.ccms.portal.dto.response.TransactionResponse;
import com.ccms.portal.entity.Transaction;
import com.ccms.portal.entity.UserEntity;
import com.ccms.portal.enums.CardStatus;
import com.ccms.portal.exception.UserNotFoundException;
import com.ccms.portal.repository.CreditCardRepository;
import com.ccms.portal.repository.TransactionRepository;
import com.ccms.portal.repository.UserRepository;
import com.ccms.portal.util.JwtUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private CreditCardRepository cardRepo;

    @Autowired
    private TransactionRepository txRepo;

    @Autowired
    private UserRepository userRepo;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;

        Long userId;
        if (principal instanceof JwtUserDetails) {
            userId = ((JwtUserDetails) principal).getUserId();
        } else {
            userId = null;
        }

        if (userId == null) {
            throw new IllegalStateException("Authenticated user not found. Make sure request contains a valid JWT.");
        }

        UserEntity userEntity = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + userId));

        Double totalLimitObj = cardRepo.sumCreditLimitByUserId(userId);
        double totalLimit = totalLimitObj == null ? 0.0 : totalLimitObj;

        Double availableObj = cardRepo.sumAvailableLimitByUserId(userId);
        double availableCredit = availableObj == null ? 0.0 : availableObj;

        double outstanding = totalLimit - availableCredit;

        int totalCards = (int) cardRepo.countByUserId(userId);
        int activeCards = (int) cardRepo.countByUserIdAndStatus(userId, CardStatus.ACTIVE);

        SummaryResponse summary = new SummaryResponse();
        summary.setActiveCards(activeCards);
        summary.setTotalCards(totalCards);
        summary.setTotalLimit(totalLimit);
        summary.setAvailableCredit(availableCredit);
        summary.setOutstanding(outstanding);

        List<Transaction> txEntities = txRepo.findTop10ByUserId(userId);
        List<TransactionResponse> transactions = txEntities.stream()
                .map(t -> {
                    TransactionResponse tr = new TransactionResponse();
                    tr.setId(t.getId() != null ? t.getId().toString() : null);
                    tr.setCardId(t.getCard() != null && t.getCard().getId() != null
                            ? String.valueOf(t.getCard().getId()) : null);
                    tr.setAmount(t.getAmount() != null ? t.getAmount().doubleValue() : 0.0);
                    tr.setMerchant(t.getMerchantAccount() != null ? String.valueOf(t.getMerchantAccount()) : null);
                    tr.setCategory(t.getNetwork());
                    tr.setMode(t.getProcessor() != null ? String.valueOf(t.getProcessor()) : null);
                    tr.setDate(t.getCreatedAt() != null
                            ? t.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()
                            : null);
                    return tr;
                })
                .sorted(Comparator.comparing(TransactionResponse::getDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .collect(Collectors.toList());

        DashboardResponse dashboard = new DashboardResponse();
        dashboard.setUserName(userEntity.getName());
        dashboard.setSummary(summary);
        dashboard.setTransactions(transactions);
        dashboard.setLastUpdated(Instant.now());

        return dashboard;
    }
}
