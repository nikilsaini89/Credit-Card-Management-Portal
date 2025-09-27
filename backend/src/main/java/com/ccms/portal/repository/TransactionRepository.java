package com.ccms.portal.repository;

import com.ccms.portal.entity.Transaction;
import com.ccms.portal.entity.Transaction.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

        // Find transactions by card ID
        List<Transaction> findByCardIdOrderByCreatedAtDesc(Long cardId);

        // Find transactions by card ID with pagination
        Page<Transaction> findByCardIdOrderByCreatedAtDesc(Long cardId, Pageable pageable);

        // Find transactions by user ID (through card relationship)
        @Query("SELECT t FROM Transaction t JOIN t.card c WHERE c.user.id = :userId ORDER BY t.createdAt DESC")
        List<Transaction> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

        // Find transactions by user ID with pagination
        @Query("SELECT t FROM Transaction t JOIN t.card c WHERE c.user.id = :userId ORDER BY t.createdAt DESC")
        Page<Transaction> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

        // Find transactions by status
        List<Transaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status);

        // Find transactions by category
        List<Transaction> findByCategoryOrderByCreatedAtDesc(String category);

        // Find BNPL transactions
        List<Transaction> findByIsBnplTrueOrderByCreatedAtDesc();

        // Find transactions by merchant
        List<Transaction> findByMerchantContainingIgnoreCaseOrderByCreatedAtDesc(String merchant);

        // Find transactions by date range
        List<Transaction> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

        // Find transactions by user with filters
        @Query("SELECT t FROM Transaction t JOIN t.card c WHERE c.user.id = :userId " +
                        "AND (:status IS NULL OR t.status = :status) " +
                        "AND (:category IS NULL OR t.category = :category) " +
                        "AND (:isBnpl IS NULL OR t.isBnpl = :isBnpl) " +
                        "AND (:merchant IS NULL OR LOWER(t.merchant) LIKE LOWER(CONCAT('%', :merchant, '%'))) " +
                        "ORDER BY t.createdAt DESC")
        List<Transaction> findByUserIdWithFilters(@Param("userId") Long userId,
                        @Param("status") TransactionStatus status,
                        @Param("category") String category,
                        @Param("isBnpl") Boolean isBnpl,
                        @Param("merchant") String merchant);

        // Calculate total amount by user
        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t JOIN t.card c WHERE c.user.id = :userId")
        BigDecimal getTotalAmountByUserId(@Param("userId") Long userId);

        // Calculate total amount by user and status
        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t JOIN t.card c WHERE c.user.id = :userId AND t.status = :status")
        BigDecimal getTotalAmountByUserIdAndStatus(@Param("userId") Long userId,
                        @Param("status") TransactionStatus status);

        // Get transaction count by user
        @Query("SELECT COUNT(t) FROM Transaction t JOIN t.card c WHERE c.user.id = :userId")
        Long getTransactionCountByUserId(@Param("userId") Long userId);

        // Get average transaction amount by user
        @Query("SELECT COALESCE(AVG(t.amount), 0) FROM Transaction t JOIN t.card c WHERE c.user.id = :userId")
        BigDecimal getAverageTransactionAmountByUserId(@Param("userId") Long userId);

        // Get category breakdown by user
        @Query("SELECT t.category, COUNT(t), COALESCE(SUM(t.amount), 0) FROM Transaction t JOIN t.card c " +
                        "WHERE c.user.id = :userId GROUP BY t.category ORDER BY COUNT(t) DESC")
        List<Object[]> getCategoryBreakdownByUserId(@Param("userId") Long userId);

        // Get monthly spending by user
        @Query("SELECT YEAR(t.createdAt), MONTH(t.createdAt), COALESCE(SUM(t.amount), 0) FROM Transaction t JOIN t.card c "
                        +
                        "WHERE c.user.id = :userId GROUP BY YEAR(t.createdAt), MONTH(t.createdAt) ORDER BY YEAR(t.createdAt) DESC, MONTH(t.createdAt) DESC")
        List<Object[]> getMonthlySpendingByUserId(@Param("userId") Long userId);

        // Get top merchants by user
        @Query("SELECT t.merchant, COUNT(t), COALESCE(SUM(t.amount), 0) FROM Transaction t JOIN t.card c " +
                        "WHERE c.user.id = :userId GROUP BY t.merchant ORDER BY COUNT(t) DESC")
        List<Object[]> getTopMerchantsByUserId(@Param("userId") Long userId);

        // Find max serial number
        @Query("SELECT MAX(t.serialNo) FROM Transaction t")
        Long findMaxSerialNo();
}
