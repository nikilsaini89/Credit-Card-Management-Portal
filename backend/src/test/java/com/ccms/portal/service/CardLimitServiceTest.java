package com.ccms.portal.service;

import com.ccms.portal.dto.request.UpdateCardLimitRequest;
import com.ccms.portal.dto.response.CardLimitResponse;
import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.entity.CardTypeEntity;
import com.ccms.portal.entity.UserEntity;
import com.ccms.portal.exception.CardLimitException;
import com.ccms.portal.exception.CreditCardNotFoundException;
import com.ccms.portal.exception.UnauthorizedApplicationActionException;
import com.ccms.portal.repository.CreditCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardLimitServiceTest {

    @Mock
    private CreditCardRepository creditCardRepository;

    @InjectMocks
    private CardLimitService cardLimitService;

    private CreditCardEntity card;
    private UserEntity user;
    private CardTypeEntity cardType;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setEmail("testuser@example.com");

        cardType = new CardTypeEntity();
        cardType.setMinCardLimit(1000.0);
        cardType.setMaxCardLimit(20000.0);

        card = new CreditCardEntity();
        card.setId(10L);
        card.setUser(user);
        card.setCreditLimit(10000.0);
        card.setAvailableLimit(8000.0); // outstanding = 2000
        card.setCardType(cardType);
    }

    @Test
    void testUpdateLimit_Successful() {
        UpdateCardLimitRequest request = new UpdateCardLimitRequest();
        request.setNewCreditLimit(15000.0);

        when(creditCardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(creditCardRepository.save(any(CreditCardEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardLimitResponse response = cardLimitService.updateLimit(10L, request, "testuser@example.com");

        assertNotNull(response);
        assertEquals(10L, response.getCardId());
        assertEquals(10000.0, response.getOldLimit());
        assertEquals(15000.0, response.getNewLimit());
        assertEquals(13000.0, response.getAvailableLimit()); // 15000 - outstanding(2000) = 13000

        verify(creditCardRepository, times(1)).save(card);
    }

    @Test
    void testUpdateLimit_NullRequest() {
        assertThrows(CardLimitException.class,
                () -> cardLimitService.updateLimit(10L, null, "testuser@example.com"));
    }

    @Test
    void testUpdateLimit_NullLimitValue() {
        UpdateCardLimitRequest request = new UpdateCardLimitRequest();
        request.setNewCreditLimit(null);

        assertThrows(CardLimitException.class,
                () -> cardLimitService.updateLimit(10L, request, "testuser@example.com"));
    }

    @Test
    void testUpdateLimit_CardNotFound() {
        UpdateCardLimitRequest request = new UpdateCardLimitRequest();
        request.setNewCreditLimit(12000.0);

        when(creditCardRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(CreditCardNotFoundException.class,
                () -> cardLimitService.updateLimit(10L, request, "testuser@example.com"));
    }

    @Test
    void testUpdateLimit_UnauthorizedUser() {
        UpdateCardLimitRequest request = new UpdateCardLimitRequest();
        request.setNewCreditLimit(12000.0);

        when(creditCardRepository.findById(10L)).thenReturn(Optional.of(card));

        assertThrows(UnauthorizedApplicationActionException.class,
                () -> cardLimitService.updateLimit(10L, request, "wronguser@example.com"));
    }

    @Test
    void testUpdateLimit_BelowMinLimit() {
        UpdateCardLimitRequest request = new UpdateCardLimitRequest();
        request.setNewCreditLimit(500.0); // below min limit (1000)

        when(creditCardRepository.findById(10L)).thenReturn(Optional.of(card));

        assertThrows(CardLimitException.class,
                () -> cardLimitService.updateLimit(10L, request, "testuser@example.com"));
    }

    @Test
    void testUpdateLimit_AboveMaxLimit() {
        UpdateCardLimitRequest request = new UpdateCardLimitRequest();
        request.setNewCreditLimit(25000.0); // above max limit (20000)

        when(creditCardRepository.findById(10L)).thenReturn(Optional.of(card));

        assertThrows(CardLimitException.class,
                () -> cardLimitService.updateLimit(10L, request, "testuser@example.com"));
    }

    @Test
    void testUpdateLimit_LessThanOutstanding() {
        UpdateCardLimitRequest request = new UpdateCardLimitRequest();
        request.setNewCreditLimit(1500.0); // outstanding = 2000

        when(creditCardRepository.findById(10L)).thenReturn(Optional.of(card));

        assertThrows(CardLimitException.class,
                () -> cardLimitService.updateLimit(10L, request, "testuser@example.com"));
    }

    @Test
    void testUpdateLimit_OwnerCheckWithNullUserOrCaller() {
        // Card without user
        card.setUser(null);
        UpdateCardLimitRequest request = new UpdateCardLimitRequest();
        request.setNewCreditLimit(12000.0);

        when(creditCardRepository.findById(10L)).thenReturn(Optional.of(card));

        assertThrows(UnauthorizedApplicationActionException.class,
                () -> cardLimitService.updateLimit(10L, request, "testuser@example.com"));
    }
}
