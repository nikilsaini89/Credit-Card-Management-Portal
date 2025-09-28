package com.ccms.portal.repository;

import com.ccms.portal.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("SELECT t FROM Transaction t WHERE t.card.user.id = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findTop10ByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.card.user.id = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findAllByUserId(@Param("userId") Long userId);
}
