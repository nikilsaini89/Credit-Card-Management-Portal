package com.ccms.portal.service;

import com.ccms.portal.dto.response.CardResponse;
import com.ccms.portal.dto.response.DashboardResponse;
import com.ccms.portal.dto.response.SummaryResponse;
import com.ccms.portal.dto.response.TransactionResponse;
import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.entity.Transaction;
import com.ccms.portal.repository.CreditCardRepository;
import com.ccms.portal.repository.TransactionRepository;
import com.ccms.portal.util.MaskUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
    CreditCardRepository cardRepo;

    @Autowired
    TransactionRepository txRepo;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        List<CreditCardEntity> cardEntities = cardRepo.findAll();
        List<Transaction> txEntities = txRepo.findAll();

        List<CardResponse> cards = cardEntities.stream()
                .map(c -> {
                    CardResponse r = new CardResponse();
                    r.setId(c.getId() == null ? null : c.getId().toString());
                    try {
                        r.setName(c.getCardType() == null ? "Card" : c.getCardType().getName());
                    } catch (Exception ignored) {
                        r.setName("Card");
                    }
                    r.setNumber(MaskUtil.maskCardNumber(c.getCardNumber()));
                    r.setTotalLimit(c.getCreditLimit() == null ? 0.0 : c.getCreditLimit());
                    r.setAvailableLimit(c.getAvailableLimit() == null ? 0.0 : c.getAvailableLimit());
                    try {
                        r.setStatus(c.getCardStatus() == null ? null : c.getCardStatus().name());
                    } catch (Exception ignored) {
                        r.setStatus(null);
                    }
                    return r;
                })
                .collect(Collectors.toList());

        double totalLimit = cards.stream()
                .mapToDouble(c -> c.getTotalLimit() == null ? 0.0 : c.getTotalLimit())
                .sum();

        double availableCredit = cards.stream()
                .mapToDouble(c -> c.getAvailableLimit() == null ? 0.0 : c.getAvailableLimit())
                .sum();

        double outstanding = totalLimit - availableCredit;

        int totalCards = cards.size();
        int activeCards = (int) cards.stream()
                .filter(c -> c.getStatus() != null && c.getStatus().equalsIgnoreCase("ACTIVE"))
                .count();

        SummaryResponse summary = new SummaryResponse();
        summary.setActiveCards(activeCards);
        summary.setTotalCards(totalCards);
        summary.setTotalLimit(totalLimit);
        summary.setAvailableCredit(availableCredit);
        summary.setOutstanding(outstanding);

        List<TransactionResponse> transactions = txEntities.stream()
                .map(t -> {
                    TransactionResponse tr = new TransactionResponse();
                    tr.setId(t.getId() == null ? null : t.getId().toString());
                    try {
                        tr.setCardId(t.getCard() == null || t.getCard().getId() == null ? null :
                                t.getCard().getId().toString());
                    } catch (Exception ignored) {
                        tr.setCardId(null);
                    }
                    try {
                        tr.setAmount(t.getAmount() == null ? 0.0 : t.getAmount().doubleValue());
                    } catch (Exception ignored) {
                        tr.setAmount(0.0);
                    }
                    try {
                        tr.setMerchant(t.getMerchantAccount() == null ? null :
                                String.valueOf(t.getMerchantAccount()));
                    } catch (Exception ignored) {
                        tr.setMerchant(null);
                    }
                    tr.setCategory(t.getNetwork());
                    try {
                        tr.setMode(t.getProcessor() == null ? null : String.valueOf(t.getProcessor()));
                    } catch (Exception ignored) {
                        tr.setMode(null);
                    }
                    try {
                        if (t.getCreatedAt() != null) {
                            tr.setDate(t.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
                        } else {
                            tr.setDate(null);
                        }
                    } catch (Exception ignored) {
                        tr.setDate(null);
                    }
                    return tr;
                })
                .sorted(Comparator.comparing(TransactionResponse::getDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .collect(Collectors.toList());

        DashboardResponse dashboard = new DashboardResponse();
        dashboard.setUserName("User");
        dashboard.setSummary(summary);
        dashboard.setCards(cards);
        dashboard.setTransactions(transactions);
        dashboard.setLastUpdated(Instant.now());

        return dashboard;
    }
}
