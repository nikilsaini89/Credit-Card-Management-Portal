package com.ccms.portal.service;

import com.ccms.portal.dto.request.CreateTransactionRequest;
import com.ccms.portal.dto.response.AnalyticsResponse;
import com.ccms.portal.dto.response.TransactionHistoryResponse;
import com.ccms.portal.dto.response.TransactionResponse;
import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.entity.Transaction;
import com.ccms.portal.entity.UserProfileEntity;
import com.ccms.portal.enums.TransactionStatus;
import com.ccms.portal.exception.*;
import com.ccms.portal.repository.CreditCardRepository;
import com.ccms.portal.repository.TransactionRepository;
import com.ccms.portal.repository.UserProfileRepository;
import com.ccms.portal.util.JwtUserDetails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CreditCardRepository creditCardRepository;
    @Mock
    private CreditCardStatementService creditCardStatementService;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private CreditLimitService creditLimitService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private TransactionService transactionService;

    private CreditCardEntity card;
    private Transaction transaction;
    private JwtUserDetails userDetails;
    private UserProfileEntity userProfile;

    @BeforeEach
    void setUp() {
        card = new CreditCardEntity();
        card.setId(100L);
        card.setCreditLimit(5000.0);
        card.setAvailableLimit(5000.0);

        transaction = new Transaction();
        transaction.setId(200L);
        transaction.setCard(card);
        transaction.setAmount(BigDecimal.valueOf(1000));
        transaction.setCategory("SHOPPING");
        transaction.setIsBnpl(false);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setTransactionDate(LocalDate.now());

        userDetails = new JwtUserDetails(1L, "testUser", "ROLE_USER");
        card.setUser(new com.ccms.portal.entity.UserEntity());
        card.getUser().setId(1L);

        userProfile = new UserProfileEntity();
        userProfile.setEligibleBnpl(true);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void testCreateTransaction_SuccessNormal() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setCardId(100L);
        request.setAmount(BigDecimal.valueOf(1000));
        request.setMerchantName("Amazon");
        request.setTransactionDate(LocalDate.now());
        request.setIsBnpl(false);
        request.setCategory("SHOPPING");

        when(creditCardRepository.findById(100L)).thenReturn(Optional.of(card));
        when(creditLimitService.hasSufficientCreditLimit(100L, BigDecimal.valueOf(1000))).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse resp = transactionService.createTransaction(request);

        assertNotNull(resp);
        assertEquals("SHOPPING", resp.getCategory());
        verify(creditCardStatementService, times(1)).createCurrentStatement(100L);
        verify(creditLimitService, times(1)).recalculateAvailableLimit(100L);
    }

    @Test
    void testCreateTransaction_InsufficientLimit() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setCardId(100L);
        request.setAmount(BigDecimal.valueOf(6000));
        request.setMerchantName("Amazon");

        when(creditCardRepository.findById(100L)).thenReturn(Optional.of(card));
        when(creditLimitService.hasSufficientCreditLimit(anyLong(), any())).thenReturn(false);
        when(creditLimitService.calculateCurrentAvailableLimit(anyLong())).thenReturn(BigDecimal.valueOf(1000));

        assertThrows(InsufficientLimitException.class,
                () -> transactionService.createTransaction(request));
    }

    @Test
    void testCreateTransaction_InvalidAmount() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setCardId(100L);
        request.setAmount(BigDecimal.ZERO);
        request.setMerchantName("Flipkart");
        request.setIsBnpl(false);

        when(creditCardRepository.findById(100L)).thenReturn(Optional.of(card));

        // 👇 IMPORTANT: allow it to pass credit-limit check
        when(creditLimitService.hasSufficientCreditLimit(eq(100L), eq(BigDecimal.ZERO)))
                .thenReturn(true);

        assertThrows(InvalidTransactionException.class,
                () -> transactionService.createTransaction(request));
    }
    @Test
    void testCreateTransaction_BnplNotEligible() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setCardId(100L);
        request.setAmount(BigDecimal.valueOf(1000));
        request.setMerchantName("Amazon");
        request.setIsBnpl(true);

        userProfile.setEligibleBnpl(false);

        when(creditCardRepository.findById(100L)).thenReturn(Optional.of(card));
        when(creditLimitService.hasSufficientCreditLimit(anyLong(), any())).thenReturn(true);
        when(userProfileRepository.findByUser(any())).thenReturn(Optional.of(userProfile));

        assertThrows(BnplNotEligibleException.class,
                () -> transactionService.createTransaction(request));
    }

    @Test
    void testGetTransactionHistory_Success() {
        Page<Transaction> transactionPage = new PageImpl<>(Collections.singletonList(transaction));
        when(creditCardRepository.findById(100L)).thenReturn(Optional.of(card));
        when(transactionRepository.findByCardIdOrderByTransactionDateDesc(eq(100L), any(Pageable.class)))
                .thenReturn(transactionPage);

        TransactionHistoryResponse resp = transactionService.getTransactionHistory(100L, 0, 10, null, null, null);

        assertNotNull(resp);
        assertEquals(1, resp.getTransactions().size());
        assertEquals("SHOPPING", resp.getTransactions().get(0).getCategory());
    }

    @Test
    void testGetTransactionHistory_Unauthorized() {
        card.getUser().setId(2L); // Different user
        when(creditCardRepository.findById(100L)).thenReturn(Optional.of(card));

        assertThrows(UnauthorizedException.class,
                () -> transactionService.getTransactionHistory(100L, 0, 10, null, null, null));
    }

    @Test
    void testGetSpendingAnalytics_Success() {
        when(creditCardRepository.findById(100L)).thenReturn(Optional.of(card));
        when(transactionRepository.getTotalSpentByCardId(100L)).thenReturn(3000.0);
        when(transactionRepository.countByCardId(100L)).thenReturn(3L);
        when(transactionRepository.findByCardIdAndIsBnplTrueOrderByTransactionDateDesc(100L))
                .thenReturn(Collections.emptyList());

        // FIX: Use explicit List<Object[]> for category & monthly trends
        Object[] categoryRow = new Object[]{"SHOPPING", BigDecimal.valueOf(1200)};
        List<Object[]> categoryList = new ArrayList<>();
        categoryList.add(categoryRow);
        when(transactionRepository.getSpendingByCategory(100L)).thenReturn(categoryList);

        Object[] monthlyRow = new Object[]{2025, 9, BigDecimal.valueOf(2000)};
        List<Object[]> monthlyList = new ArrayList<>();
        monthlyList.add(monthlyRow);
        when(transactionRepository.getMonthlySpendingTrend(eq(100L), any(LocalDate.class)))
                .thenReturn(monthlyList);

        AnalyticsResponse resp = transactionService.getSpendingAnalytics(100L);

        assertNotNull(resp);
        assertEquals(BigDecimal.valueOf(3000.0), resp.getTotalSpent());
        assertEquals(1, resp.getCategoryBreakdown().size());
        assertEquals(1, resp.getMonthlyTrends().size());
        assertTrue(resp.getAverageTransactionAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testGetTransactionById_Success() {
        when(transactionRepository.findById(200L)).thenReturn(Optional.of(transaction));

        TransactionResponse resp = transactionService.getTransactionById(200L);

        assertNotNull(resp);
        assertEquals("SHOPPING", resp.getCategory());
    }

    @Test
    void testGetTransactionById_NotFound() {
        when(transactionRepository.findById(300L)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getTransactionById(300L));
    }

    @Test
    void testGetBnplTransactions_Success() {
        when(transactionRepository.findByCardIdAndIsBnplTrueOrderByTransactionDateDesc(100L))
                .thenReturn(Collections.singletonList(transaction));

        List<TransactionResponse> resp = transactionService.getBnplTransactions(100L);

        assertNotNull(resp);
        assertEquals(1, resp.size());
    }

    @Test
    void testGetMonthlySpendingTrends_Success() {
        when(creditCardRepository.findById(100L)).thenReturn(Optional.of(card));

        Object[] monthlyRow = new Object[]{2025, 9, BigDecimal.valueOf(2000)};
        List<Object[]> monthlyList = new ArrayList<>();
        monthlyList.add(monthlyRow);

        when(transactionRepository.getMonthlySpendingTrend(eq(100L), any(LocalDate.class)))
                .thenReturn(monthlyList);

        List<Object[]> trends = transactionService.getMonthlySpendingTrends(100L, LocalDate.now().minusMonths(6));

        assertNotNull(trends);
        assertEquals(1, trends.size());
    }
}
