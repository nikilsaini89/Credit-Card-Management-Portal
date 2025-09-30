package com.ccms.portal.service;

import com.ccms.portal.dto.request.BnplPaymentRequest;
import com.ccms.portal.dto.response.BnplOverviewResponse;
import com.ccms.portal.entity.BnplPayment;
import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.entity.Transaction;
import com.ccms.portal.enums.TransactionStatus;
import com.ccms.portal.exception.ResourceNotFoundException;
import com.ccms.portal.exception.UnauthorizedException;
import com.ccms.portal.repository.BnplPaymentRepository;
import com.ccms.portal.repository.CreditCardRepository;
import com.ccms.portal.repository.TransactionRepository;
import com.ccms.portal.util.JwtUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BnplServiceTest {

    @Mock private BnplPaymentRepository bnplPaymentRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private CreditCardRepository creditCardRepository;

    @InjectMocks
    private BnplService bnplService;

    private Authentication authentication;
    private SecurityContext securityContext;
    private JwtUserDetails userDetails;
    private CreditCardEntity card;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        // Mock SecurityContext with user
        userDetails = new JwtUserDetails(1L, "testUser", "USER");
        authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock card
        card = new CreditCardEntity();
        ReflectionTestUtils.setField(card, "id", 10L);
        card.setAvailableLimit(20_000.0);

        // Mock BNPL transaction
        transaction = new Transaction();
        ReflectionTestUtils.setField(transaction, "id", 100L);
        transaction.setCard(card);
        transaction.setIsBnpl(true);
        transaction.setAmount(BigDecimal.valueOf(6_000));
        transaction.setTenureMonths(6);
        transaction.setMerchantName("Amazon");
        transaction.setTransactionDate(LocalDate.now().minusDays(10));
        transaction.setStatus(TransactionStatus.BNPL_ACTIVE);
    }

    /** -------------------- getBnplOverview -------------------- */

    @Test
    void getBnplOverview_noBnplTransactions_returnsEmptyOverview() {
        when(creditCardRepository.existsByIdAndUserId(10L, 1L)).thenReturn(true);
        when(transactionRepository.findByCardIdAndIsBnplTrueOrderByTransactionDateDesc(10L))
                .thenReturn(Collections.emptyList());

        BnplOverviewResponse resp = bnplService.getBnplOverview(10L);

        assertEquals(BigDecimal.ZERO, resp.getOutstandingAmount());
        assertEquals(0, resp.getActivePlansCount());
    }

    @Test
    void getBnplOverview_withTransactions_andPayments() {
        when(creditCardRepository.existsByIdAndUserId(10L, 1L)).thenReturn(true);
        when(transactionRepository.findByCardIdAndIsBnplTrueOrderByTransactionDateDesc(10L))
                .thenReturn(List.of(transaction));

        when(bnplPaymentRepository.getTotalPaidAmount(100L)).thenReturn(BigDecimal.valueOf(2_000));

        // Build BnplPayment using Lombok builder
        BnplPayment payment = BnplPayment.builder()
                .id(1L)
                .transaction(transaction)
                .paymentAmount(BigDecimal.valueOf(2_000))
                .paymentDate(LocalDate.now())
                .installmentNumber(1)
                .totalInstallments(6)
                .remainingAmount(BigDecimal.valueOf(4_000))
                .status(BnplPayment.PaymentStatus.COMPLETED)
                .build();

        when(bnplPaymentRepository.findByTransactionIdOrderByInstallmentNumberAsc(100L))
                .thenReturn(List.of(payment));

        BnplOverviewResponse resp = bnplService.getBnplOverview(10L);

        assertEquals(1, resp.getActivePlansCount());
        assertEquals(BigDecimal.valueOf(6_000), resp.getTotalAmount());
        assertEquals(BigDecimal.valueOf(2_000), resp.getTotalPaidAmount());
        assertEquals(BigDecimal.valueOf(4_000), resp.getOutstandingAmount());
        assertTrue(resp.getProgressPercentage() > 0);
    }

    @Test
    void getBnplOverview_userNotAuthorized() {
        when(creditCardRepository.existsByIdAndUserId(10L, 1L)).thenReturn(false);
        assertThrows(UnauthorizedException.class, () -> bnplService.getBnplOverview(10L));
    }

    /** -------------------- makeBnplPayment -------------------- */

    @Test
    void makeBnplPayment_success() {
        when(transactionRepository.findById(100L)).thenReturn(Optional.of(transaction));
        when(creditCardRepository.existsByIdAndUserId(10L, 1L)).thenReturn(true);

        Object[] summary = {100L, BigDecimal.valueOf(6_000),
                BigDecimal.valueOf(2_000), BigDecimal.valueOf(4_000)};
        when(bnplPaymentRepository.getPaymentSummary(100L)).thenReturn(summary);
        when(bnplPaymentRepository.getNextInstallmentNumber(100L)).thenReturn(3);
        when(bnplPaymentRepository.save(any(BnplPayment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(creditCardRepository.save(any(CreditCardEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        BnplPaymentRequest req = new BnplPaymentRequest();
        req.setTransactionId(100L);
        req.setPaymentAmount(BigDecimal.valueOf(1_000));

        BnplPayment payment = bnplService.makeBnplPayment(req);

        assertNotNull(payment);
        assertEquals(BigDecimal.valueOf(1_000), payment.getPaymentAmount());
        verify(bnplPaymentRepository).save(any(BnplPayment.class));
        verify(creditCardRepository).save(any(CreditCardEntity.class));
    }

    @Test
    void makeBnplPayment_transactionNotFound() {
        when(transactionRepository.findById(100L)).thenReturn(Optional.empty());

        BnplPaymentRequest req = new BnplPaymentRequest();
        req.setTransactionId(100L);
        req.setPaymentAmount(BigDecimal.valueOf(1_000));

        assertThrows(ResourceNotFoundException.class, () -> bnplService.makeBnplPayment(req));
    }

    @Test
    void makeBnplPayment_transactionNotBnpl() {
        transaction.setIsBnpl(false);
        when(transactionRepository.findById(100L)).thenReturn(Optional.of(transaction));

        BnplPaymentRequest req = new BnplPaymentRequest();
        req.setTransactionId(100L);
        req.setPaymentAmount(BigDecimal.valueOf(1_000));

        assertThrows(IllegalArgumentException.class, () -> bnplService.makeBnplPayment(req));
    }

    @Test
    void makeBnplPayment_exceedsRemainingAmount() {
        when(transactionRepository.findById(100L)).thenReturn(Optional.of(transaction));
        when(creditCardRepository.existsByIdAndUserId(10L, 1L)).thenReturn(true);

        Object[] summary = {100L, BigDecimal.valueOf(6_000),
                BigDecimal.valueOf(5_500), BigDecimal.valueOf(500)};
        when(bnplPaymentRepository.getPaymentSummary(100L)).thenReturn(summary);

        BnplPaymentRequest req = new BnplPaymentRequest();
        req.setTransactionId(100L);
        req.setPaymentAmount(BigDecimal.valueOf(2_000)); // exceeds remaining

        assertThrows(IllegalArgumentException.class, () -> bnplService.makeBnplPayment(req));
    }

    @Test
    void makeBnplPayment_defaultTenureMonths_andFullyPaid() {
        transaction.setTenureMonths(0); // triggers default 6 months

        when(transactionRepository.findById(100L)).thenReturn(Optional.of(transaction));
        when(creditCardRepository.existsByIdAndUserId(10L, 1L)).thenReturn(true);

        Object[] summary = {100L, BigDecimal.valueOf(6_000),
                BigDecimal.valueOf(5_500), BigDecimal.valueOf(500)};
        when(bnplPaymentRepository.getPaymentSummary(100L)).thenReturn(summary);
        when(bnplPaymentRepository.getNextInstallmentNumber(100L)).thenReturn(null); // default installment=1
        when(bnplPaymentRepository.save(any(BnplPayment.class))).thenAnswer(inv -> inv.getArgument(0));

        BnplPaymentRequest req = new BnplPaymentRequest();
        req.setTransactionId(100L);
        req.setPaymentAmount(BigDecimal.valueOf(500)); // completes payment

        BnplPayment payment = bnplService.makeBnplPayment(req);

        assertEquals(BigDecimal.ZERO, payment.getRemainingAmount());
        assertEquals(1, payment.getInstallmentNumber());
        assertEquals(6, payment.getTotalInstallments());
        assertEquals(TransactionStatus.BNPL_COMPLETED, transaction.getStatus());
    }

    /** -------------------- verifyCardOwnership -------------------- */

    @Test
    void getBnplOverview_noAuth_throwsUnauthorized() {
        SecurityContextHolder.clearContext(); // remove authentication
        assertThrows(UnauthorizedException.class, () -> bnplService.getBnplOverview(10L));
    }
}
