package com.ccms.portal.service;

import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.entity.CreditCardStatement;
import com.ccms.portal.entity.Transaction;
import com.ccms.portal.enums.TransactionStatus;
import com.ccms.portal.repository.BnplPaymentRepository;
import com.ccms.portal.repository.CreditCardRepository;
import com.ccms.portal.repository.CreditCardStatementRepository;
import com.ccms.portal.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CreditLimitServiceTest {

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CreditCardStatementRepository statementRepository;

    @Mock
    private BnplPaymentRepository bnplPaymentRepository;

    @InjectMocks
    private CreditLimitService creditLimitService;

    private CreditCardEntity card;

    @BeforeEach
    void setUp() {
        card = new CreditCardEntity();
        card.setId(10L);
        card.setCreditLimit(10000.0);
        card.setAvailableLimit(10000.0);

        // Lenient stubbing for payment repo to avoid UnnecessaryStubbingException
        lenient().when(bnplPaymentRepository.getTotalPaidAmount(anyLong()))
                .thenReturn(BigDecimal.ZERO);
    }

    @Test
    void testCalculateAvailableLimit_NoStatementNoBNPL() {
        when(creditCardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(statementRepository.findFirstByCardIdOrderByStatementDateDesc(10L))
                .thenReturn(Optional.empty());
        when(transactionRepository.findByCardIdAndIsBnplTrueAndStatusIn(eq(10L), any()))
                .thenReturn(Collections.emptyList());

        BigDecimal result = creditLimitService.calculateCurrentAvailableLimit(10L);

        assertEquals(BigDecimal.valueOf(10000).stripTrailingZeros(),
                result.stripTrailingZeros());
    }

    @Test
    void testCalculateAvailableLimit_WithStatement() {
        when(creditCardRepository.findById(10L)).thenReturn(Optional.of(card));

        CreditCardStatement statement = new CreditCardStatement();
        statement.setStatementAmount(BigDecimal.valueOf(2000));
        statement.setPaidAmount(BigDecimal.ZERO);
        when(statementRepository.findFirstByCardIdOrderByStatementDateDesc(10L))
                .thenReturn(Optional.of(statement));

        when(transactionRepository.findByCardIdAndIsBnplTrueAndStatusIn(eq(10L), any()))
                .thenReturn(Collections.emptyList());

        BigDecimal result = creditLimitService.calculateCurrentAvailableLimit(10L);

        assertEquals(BigDecimal.valueOf(8000).stripTrailingZeros(),
                result.stripTrailingZeros());
    }

    @Test
    void testCalculateAvailableLimit_WithBNPLOutstanding() {
        when(creditCardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(statementRepository.findFirstByCardIdOrderByStatementDateDesc(10L))
                .thenReturn(Optional.empty());

        Transaction bnplTransaction = new Transaction();
        bnplTransaction.setId(1L);
        bnplTransaction.setAmount(BigDecimal.valueOf(2000));
        bnplTransaction.setStatus(TransactionStatus.BNPL_ACTIVE);

        when(transactionRepository.findByCardIdAndIsBnplTrueAndStatusIn(eq(10L), any()))
                .thenReturn(List.of(bnplTransaction));

        BigDecimal result = creditLimitService.calculateCurrentAvailableLimit(10L);

        // Expected: 10000 - 2000 (BNPL) = 8000
        assertEquals(BigDecimal.valueOf(8000).stripTrailingZeros(),
                result.stripTrailingZeros());
    }

    @Test
    void testCalculateAvailableLimit_WithStatementAndBNPL() {
        when(creditCardRepository.findById(10L)).thenReturn(Optional.of(card));

        CreditCardStatement statement = new CreditCardStatement();
        statement.setStatementAmount(BigDecimal.valueOf(2000));
        statement.setPaidAmount(BigDecimal.ZERO);
        when(statementRepository.findFirstByCardIdOrderByStatementDateDesc(10L))
                .thenReturn(Optional.of(statement));

        Transaction bnplTransaction = new Transaction();
        bnplTransaction.setId(1L);
        bnplTransaction.setAmount(BigDecimal.valueOf(2000));
        bnplTransaction.setStatus(TransactionStatus.BNPL_ACTIVE);

        when(transactionRepository.findByCardIdAndIsBnplTrueAndStatusIn(eq(10L), any()))
                .thenReturn(List.of(bnplTransaction));

        // Expected: 10000 - (2000 stmt + 2000 bnpl) = 6000
        BigDecimal result = creditLimitService.calculateCurrentAvailableLimit(10L);
        assertEquals(BigDecimal.valueOf(6000).stripTrailingZeros(),
                result.stripTrailingZeros());
    }

    @Test
    void testCalculateAvailableLimit_WithBNPLPaymentFlag() {
        when(creditCardRepository.findById(10L)).thenReturn(Optional.of(card));

        Transaction bnplTransaction = new Transaction();
        bnplTransaction.setId(1L);
        bnplTransaction.setAmount(BigDecimal.valueOf(2000));
        bnplTransaction.setStatus(TransactionStatus.BNPL_ACTIVE);

        when(transactionRepository.findByCardIdAndIsBnplTrueAndStatusIn(eq(10L), any()))
                .thenReturn(List.of(bnplTransaction));

        // BNPL payment flag means only BNPL amount is subtracted
        BigDecimal result = creditLimitService.calculateCurrentAvailableLimit(10L, true);

        assertEquals(BigDecimal.valueOf(8000).stripTrailingZeros(),
                result.stripTrailingZeros());
    }

    @Test
    void testRecalculateAvailableLimit() {
        when(creditCardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(statementRepository.findFirstByCardIdOrderByStatementDateDesc(10L))
                .thenReturn(Optional.empty());
        when(transactionRepository.findByCardIdAndIsBnplTrueAndStatusIn(eq(10L), any()))
                .thenReturn(Collections.emptyList());

        creditLimitService.recalculateAvailableLimit(10L);

        verify(creditCardRepository, times(1)).save(any(CreditCardEntity.class));
    }

    @Test
    void testHasSufficientCreditLimit() {
        when(creditCardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(statementRepository.findFirstByCardIdOrderByStatementDateDesc(10L))
                .thenReturn(Optional.empty());
        when(transactionRepository.findByCardIdAndIsBnplTrueAndStatusIn(eq(10L), any()))
                .thenReturn(Collections.emptyList());

        boolean sufficient = creditLimitService.hasSufficientCreditLimit(10L, BigDecimal.valueOf(5000));
        assertTrue(sufficient);
    }

    @Test
    void testCalculateAvailableLimit_CardNotFound() {
        when(creditCardRepository.findById(10L)).thenReturn(Optional.empty());

        BigDecimal result = creditLimitService.calculateCurrentAvailableLimit(10L);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testRecalculateAvailableLimit_CardNotFound() {
        when(creditCardRepository.findById(10L)).thenReturn(Optional.empty());
        creditLimitService.recalculateAvailableLimit(10L);
        verify(creditCardRepository, never()).save(any());
    }
}
