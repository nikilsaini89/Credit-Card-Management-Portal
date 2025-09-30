package com.ccms.portal.service;

import com.ccms.portal.dto.request.PaymentRequest;
import com.ccms.portal.dto.response.StatementResponse;
import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.entity.CreditCardStatement;
import com.ccms.portal.entity.Transaction;
import com.ccms.portal.exception.ResourceNotFoundException;
import com.ccms.portal.exception.UnauthorizedException;
import com.ccms.portal.repository.CreditCardRepository;
import com.ccms.portal.repository.CreditCardStatementRepository;
import com.ccms.portal.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditCardStatementServiceTest {

    @Mock private CreditCardStatementRepository statementRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private CreditCardRepository creditCardRepository;
    @Mock private CreditLimitService creditLimitService;

    @InjectMocks
    private CreditCardStatementService statementService;

    private CreditCardStatement statement;

    @BeforeEach
    void setUp() {
        statement = new CreditCardStatement();
        ReflectionTestUtils.setField(statement, "id", 1L);
        statement.setCardId(10L);
        statement.setStatementDate(LocalDate.now());
        statement.setDueDate(LocalDate.now().plusDays(10));
        statement.setStatementAmount(BigDecimal.valueOf(1000));
        statement.setPaidAmount(BigDecimal.ZERO);
        statement.setStatus("PENDING");
    }

    /** -------------------- getCurrentStatement -------------------- */

    @Test
    void getCurrentStatement_existingStatement() {
        when(creditCardRepository.existsById(10L)).thenReturn(true);
        when(statementRepository.findFirstByCardIdOrderByStatementDateDesc(10L))
                .thenReturn(Optional.of(statement));

        StatementResponse resp = statementService.getCurrentStatement(10L);

        assertNotNull(resp);
        assertEquals(1L, resp.getId());
        verify(statementRepository, never()).save(any());
    }

    @Test
    void getCurrentStatement_noExistingStatement_createsNew() {
        when(creditCardRepository.existsById(10L)).thenReturn(true);
        when(statementRepository.findFirstByCardIdOrderByStatementDateDesc(10L))
                .thenReturn(Optional.empty());
        when(transactionRepository.findByCardIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(List.of());
        when(statementRepository.findByCardIdAndStatementDateBetween(anyLong(), any(), any()))
                .thenReturn(List.of());
        when(statementRepository.save(any(CreditCardStatement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StatementResponse resp = statementService.getCurrentStatement(10L);

        assertNotNull(resp);
        assertEquals(10L, resp.getCardId());
        verify(statementRepository).save(any());
    }

    @Test
    void getCurrentStatement_cardNotFound() {
        when(creditCardRepository.existsById(10L)).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> statementService.getCurrentStatement(10L));
    }

    /** -------------------- createCurrentStatement -------------------- */

    @Test
    void createCurrentStatement_updatesExistingStatement() {
        when(creditCardRepository.existsById(10L)).thenReturn(true);

        Transaction t1 = new Transaction();
        t1.setAmount(BigDecimal.valueOf(200));
        t1.setIsBnpl(false);

        Transaction t2 = new Transaction();
        t2.setAmount(BigDecimal.valueOf(100));
        t2.setIsBnpl(true); // excluded

        when(transactionRepository.findByCardIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(List.of(t1, t2));

        // existing statement already present
        CreditCardStatement existing = new CreditCardStatement();
        existing.setCardId(10L);
        existing.setStatementDate(LocalDate.now());
        existing.setStatementAmount(BigDecimal.valueOf(500));

        when(statementRepository.findByCardIdAndStatementDateBetween(anyLong(), any(), any()))
                .thenReturn(List.of(existing));
        when(statementRepository.save(any(CreditCardStatement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CreditCardStatement result = statementService.createCurrentStatement(10L);

        assertEquals(BigDecimal.valueOf(200), result.getStatementAmount()); // only non-BNPL included
        verify(statementRepository).save(existing);
    }

    @Test
    void createCurrentStatement_createsNewIfNotExist() {
        when(creditCardRepository.existsById(10L)).thenReturn(true);
        when(transactionRepository.findByCardIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(List.of());
        when(statementRepository.findByCardIdAndStatementDateBetween(anyLong(), any(), any()))
                .thenReturn(List.of());
        when(statementRepository.save(any(CreditCardStatement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CreditCardStatement result = statementService.createCurrentStatement(10L);

        assertEquals(BigDecimal.ZERO, result.getStatementAmount());
        assertEquals("PENDING", result.getStatus());
        verify(statementRepository).save(any());
    }

    /** -------------------- makePayment -------------------- */

    @Test
    void makePayment_fullPaymentOnTime() {
        when(statementRepository.findById(1L)).thenReturn(Optional.of(statement));
        when(creditCardRepository.existsById(10L)).thenReturn(true);
        when(statementRepository.save(any(CreditCardStatement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PaymentRequest req = new PaymentRequest();
        req.setStatementId(1L);
        req.setPaymentAmount(BigDecimal.valueOf(1000));

        StatementResponse resp = statementService.makePayment(req);

        assertEquals("PAID", resp.getStatus());
        verify(creditLimitService).recalculateAvailableLimit(10L);
    }

    @Test
    void makePayment_partialPaymentBeforeDue() {
        when(statementRepository.findById(1L)).thenReturn(Optional.of(statement));
        when(creditCardRepository.existsById(10L)).thenReturn(true);
        when(statementRepository.save(any(CreditCardStatement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PaymentRequest req = new PaymentRequest();
        req.setStatementId(1L);
        req.setPaymentAmount(BigDecimal.valueOf(400));

        StatementResponse resp = statementService.makePayment(req);

        assertEquals("PARTIAL", resp.getStatus());
        assertEquals(BigDecimal.valueOf(400), resp.getPaidAmount());
    }

    @Test
    void makePayment_overduePartialPayment() {
        // set due date to past
        statement.setDueDate(LocalDate.now().minusDays(2));
        when(statementRepository.findById(1L)).thenReturn(Optional.of(statement));
        when(creditCardRepository.existsById(10L)).thenReturn(true);
        when(statementRepository.save(any(CreditCardStatement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PaymentRequest req = new PaymentRequest();
        req.setStatementId(1L);
        req.setPaymentAmount(BigDecimal.valueOf(500));

        StatementResponse resp = statementService.makePayment(req);

        assertEquals("OVERDUE", resp.getStatus());
    }

    @Test
    void makePayment_paymentExceedsAmountDue() {
        when(statementRepository.findById(1L)).thenReturn(Optional.of(statement));
        when(creditCardRepository.existsById(10L)).thenReturn(true);

        PaymentRequest req = new PaymentRequest();
        req.setStatementId(1L);
        req.setPaymentAmount(BigDecimal.valueOf(2000)); // exceeds due

        assertThrows(IllegalArgumentException.class,
                () -> statementService.makePayment(req));
    }

    @Test
    void makePayment_statementNotFound() {
        when(statementRepository.findById(1L)).thenReturn(Optional.empty());

        PaymentRequest req = new PaymentRequest();
        req.setStatementId(1L);
        req.setPaymentAmount(BigDecimal.valueOf(500));

        assertThrows(ResourceNotFoundException.class,
                () -> statementService.makePayment(req));
    }

    /** -------------------- getStatementHistory -------------------- */

    @Test
    void getStatementHistory_success() {
        when(creditCardRepository.existsById(10L)).thenReturn(true);
        when(statementRepository.findByCardIdOrderByStatementDateDesc(10L))
                .thenReturn(List.of(statement));

        List<StatementResponse> responses = statementService.getStatementHistory(10L);

        assertEquals(1, responses.size());
        assertEquals(10L, responses.get(0).getCardId());
    }

    @Test
    void getStatementHistory_cardNotFound() {
        when(creditCardRepository.existsById(10L)).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> statementService.getStatementHistory(10L));
    }

    /** -------------------- updateStatementWithBnplPayment -------------------- */

    @Test
    void updateStatementWithBnplPayment_shouldNotThrow() {
        assertDoesNotThrow(() ->
                statementService.updateStatementWithBnplPayment(10L, BigDecimal.valueOf(1000)));
    }

    /** -------------------- applyLatePaymentPenalties -------------------- */

    @Test
    void makePayment_fullPaymentLate_shouldApplyPenalty() {
        // set due date in past → late
        statement.setDueDate(LocalDate.now().minusDays(2));

        when(statementRepository.findById(1L)).thenReturn(Optional.of(statement));
        when(creditCardRepository.existsById(10L)).thenReturn(true);
        when(statementRepository.save(any(CreditCardStatement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // mock card for penalty calculation
        CreditCardEntity card = new CreditCardEntity();
        card.setCreditLimit(50_000.0);
        card.setAvailableLimit(45_000.0);
        when(creditCardRepository.findById(10L)).thenReturn(Optional.of(card));

        PaymentRequest req = new PaymentRequest();
        req.setStatementId(1L);
        req.setPaymentAmount(BigDecimal.valueOf(1000));

        StatementResponse resp = statementService.makePayment(req);

        assertEquals("PAID", resp.getStatus());
        // After penalty, credit limit reduced
        verify(creditCardRepository).save(card);
    }
}
