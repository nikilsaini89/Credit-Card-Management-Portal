package com.ccms.portal.service;

import com.ccms.portal.dto.request.UpdateCardLimitRequest;
import com.ccms.portal.dto.response.CardLimitResponse;
import com.ccms.portal.entity.CardTypeEntity;
import com.ccms.portal.entity.CreditCardEntity;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardLimitServiceTest {

    @Mock
    private CreditCardRepository creditCardRepository;

    @InjectMocks
    private CardLimitService cardLimitService;

    private CreditCardEntity card;
    private UserEntity owner;
    private CardTypeEntity cardType;

    @BeforeEach
    void setUp() {
        // Set up a user
        owner = new UserEntity();
        owner.setEmail("owner@example.com");

        // Set up card type with limits using ReflectionTestUtils (no setters in entity)
        cardType = new CardTypeEntity();
        ReflectionTestUtils.setField(cardType, "minCardLimit", 5_000.0);
        ReflectionTestUtils.setField(cardType, "maxCardLimit", 50_000.0);

        // Set up credit card
        card = new CreditCardEntity();
        ReflectionTestUtils.setField(card, "id", 1L);
        card.setUser(owner);
        card.setCardType(cardType);
        card.setCreditLimit(20_000.0);
        card.setAvailableLimit(15_000.0);  // outstanding = 5_000
    }

    /** -------------------- Happy path -------------------- */

    @Test
    void updateLimit_success() {
        UpdateCardLimitRequest request = new UpdateCardLimitRequest();
        request.setNewCreditLimit(30_000.0); // valid within range

        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(creditCardRepository.save(any(CreditCardEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CardLimitResponse response = cardLimitService.updateLimit(1L, request, "owner@example.com");

        assertNotNull(response);
        assertEquals(20_000.0, response.getOldLimit());
        assertEquals(30_000.0, response.getNewLimit());
        assertEquals(25_000.0, response.getAvailableLimit()); // 30k - outstanding(5k)
        verify(creditCardRepository).save(card);
    }

    /** -------------------- Exception branches -------------------- */

    @Test
    void updateLimit_requestIsNull() {
        assertThrows(CardLimitException.class,
                () -> cardLimitService.updateLimit(1L, null, "owner@example.com"));
    }

    @Test
    void updateLimit_newCreditLimitIsNull() {
        UpdateCardLimitRequest request = new UpdateCardLimitRequest(); // no limit set
        assertThrows(CardLimitException.class,
                () -> cardLimitService.updateLimit(1L, request, "owner@example.com"));
    }

    @Test
    void updateLimit_cardNotFound() {
        UpdateCardLimitRequest request = new UpdateCardLimitRequest();
        request.setNewCreditLimit(30_000.0);

        when(creditCardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CreditCardNotFoundException.class,
                () -> cardLimitService.updateLimit(1L, request, "owner@example.com"));
    }

    @Test
    void updateLimit_notOwner() {
        UpdateCardLimitRequest request = new UpdateCardLimitRequest();
        request.setNewCreditLimit(30_000.0);

        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(UnauthorizedApplicationActionException.class,
                () -> cardLimitService.updateLimit(1L, request, "intruder@example.com"));
    }

    @Test
    void updateLimit_belowMinLimit() {
        UpdateCardLimitRequest request = new UpdateCardLimitRequest();
        request.setNewCreditLimit(3_000.0); // below minCardLimit (5,000)

        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(CardLimitException.class,
                () -> cardLimitService.updateLimit(1L, request, "owner@example.com"));
    }

    @Test
    void updateLimit_aboveMaxLimit() {
        UpdateCardLimitRequest request = new UpdateCardLimitRequest();
        request.setNewCreditLimit(100_000.0); // above maxCardLimit (50,000)

        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(CardLimitException.class,
                () -> cardLimitService.updateLimit(1L, request, "owner@example.com"));
    }

    @Test
    void updateLimit_belowOutstanding() {
        UpdateCardLimitRequest request = new UpdateCardLimitRequest();
        request.setNewCreditLimit(4_500.0); // outstanding = 5,000 → invalid

        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(CardLimitException.class,
                () -> cardLimitService.updateLimit(1L, request, "owner@example.com"));
    }

    /** -------------------- Edge case: no cardType -------------------- */

    @Test
    void updateLimit_noCardType_defaultsToZeroAndMax() {
        // remove cardType
        card.setCardType(null);

        UpdateCardLimitRequest request = new UpdateCardLimitRequest();
        request.setNewCreditLimit(40_000.0); // valid, defaults to [0, MAX]

        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(creditCardRepository.save(any(CreditCardEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CardLimitResponse response = cardLimitService.updateLimit(1L, request, "owner@example.com");

        assertEquals(20_000.0, response.getOldLimit());
        assertEquals(40_000.0, response.getNewLimit());
        verify(creditCardRepository).save(card);
    }

    /** -------------------- Edge case: null owner email -------------------- */

    @Test
    void updateLimit_nullOwnerEmail_unauthorized() {
        card.getUser().setEmail(null); // owner email is null

        UpdateCardLimitRequest request = new UpdateCardLimitRequest();
        request.setNewCreditLimit(30_000.0);

        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(UnauthorizedApplicationActionException.class,
                () -> cardLimitService.updateLimit(1L, request, "owner@example.com"));
    }

    @Test
    void updateLimit_nullCallerUsername_unauthorized() {
        UpdateCardLimitRequest request = new UpdateCardLimitRequest();
        request.setNewCreditLimit(30_000.0);

        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(UnauthorizedApplicationActionException.class,
                () -> cardLimitService.updateLimit(1L, request, null));
    }
}
