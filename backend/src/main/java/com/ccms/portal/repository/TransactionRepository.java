package com.ccms.portal.repository;

import com.ccms.portal.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // Fetch all transactions for a specific card (card id is Long in CreditCardEntity)
    List<Transaction> findByCard_IdOrderByCreatedAtDesc(Long cardId);

    // Fetch last 10 transactions for a card
    List<Transaction> findTop10ByCard_IdOrderByCreatedAtDesc(Long cardId);
}
