package com.ccms.portal.service;

import com.ccms.portal.dto.response.DashboardResponse;
import com.ccms.portal.dto.response.SummaryResponse;
import com.ccms.portal.dto.response.TransactionResponse;
import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.entity.Transaction;
import com.ccms.portal.entity.UserEntity;
import com.ccms.portal.enums.CardStatus;
import com.ccms.portal.enums.TransactionStatus;
import com.ccms.portal.exception.UserNotFoundException;
import com.ccms.portal.repository.CreditCardRepository;
import com.ccms.portal.repository.TransactionRepository;
import com.ccms.portal.repository.UserRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private CreditCardRepository cardRepo;

    @Mock
    private TransactionRepository txRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private DashboardService dashboardService;

    private UserEntity user;
    private JwtUserDetails userDetails;
    private CreditCardEntity card;
    private Transaction tx1, tx2;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(1L);
        user.setName("Sumit");

        userDetails = new JwtUserDetails(1L, "sumit@example.com", "USER");

        card = new CreditCardEntity();
        card.setId(10L);
        card.setCreditLimit(10000.0);
        card.setAvailableLimit(7000.0);

        tx1 = new Transaction();
        tx1.setId(101L);
        tx1.setCard(card);
        tx1.setMerchantName("Amazon");
        tx1.setAmount(BigDecimal.valueOf(1000));
        tx1.setCategory("Shopping");
        tx1.setIsBnpl(false);
        tx1.setCardType("VISA");
        tx1.setLastFour("1234");
        tx1.setStatus(TransactionStatus.COMPLETED);
        tx1.setCreatedAt(LocalDateTime.now().minusDays(1));

        tx2 = new Transaction();
        tx2.setId(102L);
        tx2.setCard(card);
        tx2.setMerchantName("Flipkart");
        tx2.setAmount(BigDecimal.valueOf(2000));
        tx2.setCategory("Electronics");
        tx2.setIsBnpl(true);
        tx2.setCardType("MASTERCARD");
        tx2.setLastFour("5678");
        tx2.setStatus(TransactionStatus.BNPL_ACTIVE);
        tx2.setCreatedAt(LocalDateTime.now());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);

        // Use lenient for all default stubs
        lenient().when(cardRepo.sumCreditLimitByUserId(1L)).thenReturn(10000.0);
        lenient().when(cardRepo.sumAvailableLimitByUserId(1L)).thenReturn(7000.0);
        lenient().when(cardRepo.countByUserId(1L)).thenReturn(2L);
        lenient().when(cardRepo.countByUserIdAndStatus(1L, CardStatus.ACTIVE)).thenReturn(1L);
        lenient().when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        lenient().when(txRepo.findTop10ByUserId(1L)).thenReturn(List.of(tx1, tx2));
    }

    @Test
    void testGetDashboard_Success() {
        DashboardResponse dashboard = dashboardService.getDashboard();

        assertNotNull(dashboard);
        assertEquals("Sumit", dashboard.getUserName());

        SummaryResponse summary = dashboard.getSummary();
        assertEquals(2, summary.getTotalCards());
        assertEquals(1, summary.getActiveCards());
        assertEquals(10000.0, summary.getTotalLimit());
        assertEquals(7000.0, summary.getAvailableCredit());
        assertEquals(3000.0, summary.getOutstanding());

        assertEquals(2, dashboard.getTransactions().size());
    }

    @Test
    void testGetDashboard_UserNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> dashboardService.getDashboard());
    }


    @Test
    void testGetDashboard_NoTransactions() {
        when(txRepo.findTop10ByUserId(1L)).thenReturn(List.of());

        DashboardResponse dashboard = dashboardService.getDashboard();
        assertEquals(0, dashboard.getTransactions().size());
    }

    @Test
    void testGetDashboard_NullLimits() {
        when(cardRepo.sumCreditLimitByUserId(1L)).thenReturn(null);
        when(cardRepo.sumAvailableLimitByUserId(1L)).thenReturn(null);

        DashboardResponse dashboard = dashboardService.getDashboard();

        assertEquals(0.0, dashboard.getSummary().getTotalLimit());
        assertEquals(0.0, dashboard.getSummary().getAvailableCredit());
        assertEquals(0.0, dashboard.getSummary().getOutstanding());
    }
}
