package com.ccms.portal.service;

import com.ccms.portal.dto.request.CreateCardRequest;
import com.ccms.portal.dto.request.UpdateCardStatusRequest;
import com.ccms.portal.dto.response.CreditCardResponse;
import com.ccms.portal.entity.CardTypeEntity;
import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.entity.UserEntity;
import com.ccms.portal.enums.CardStatus;
import com.ccms.portal.exception.CardTypeNotFoundException;
import com.ccms.portal.exception.CreditCardNotFoundException;
import com.ccms.portal.exception.UnauthorizedApplicationActionException;
import com.ccms.portal.exception.UserNotFoundException;
import com.ccms.portal.repository.CardTypeRepository;
import com.ccms.portal.repository.CreditCardRepository;
import com.ccms.portal.repository.UserRepository;
import com.ccms.portal.service.factory.CardFactory;
import com.ccms.portal.util.CreditCardUtil;
import com.ccms.portal.util.JwtUserDetails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardServiceTest {

    @Mock private CreditCardRepository cardRepository;
    @Mock private UserRepository userRepository;
    @Mock private CardTypeRepository cardTypeRepository;
    @Mock private CreditCardUtil cardHelper;

    // 🔴 Add this mock for the missing dependency
    @Mock private CardFactory defaultCardFactory;

    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private CardService cardService;

    private JwtUserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock current user in SecurityContext
        userDetails = new JwtUserDetails(1L, "testuser", "USER");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetAllCardsByUserId_Success() {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", 1L);

        CreditCardEntity card = new CreditCardEntity();
        ReflectionTestUtils.setField(card, "id", 100L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.findAllByUserIdWithCardType(1L))
                .thenReturn(List.of(card));
        when(cardHelper.buildCreditCardResponse(any(CreditCardEntity.class)))
                .thenReturn(mock(CreditCardResponse.class));

        List<CreditCardResponse> responses = cardService.getAllCardsByUserId();

        assertEquals(1, responses.size());
        verify(cardRepository).findAllByUserIdWithCardType(1L);
    }

    @Test
    void testCreateCard_Success() {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", 1L);

        CardTypeEntity cardType = new CardTypeEntity();
        ReflectionTestUtils.setField(cardType, "id", 10L);

        CreateCardRequest request = mock(CreateCardRequest.class);
        when(request.getUserId()).thenReturn(1L);
        when(request.getCardTypeId()).thenReturn(10L);
        when(request.getCreditLimit()).thenReturn(5000.0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardTypeRepository.findById(10L)).thenReturn(Optional.of(cardType));

        // Mock factory and other utils
        CreditCardEntity createdCard = new CreditCardEntity();
        when(defaultCardFactory.createCard(eq(user), eq(cardType), eq(request)))
                .thenReturn(createdCard);

        when(cardHelper.generateCardNumber()).thenReturn("1234567890123456");
        when(cardHelper.generateExpiryDate(anyInt()))
                .thenReturn(new Date(System.currentTimeMillis()));

        CreditCardEntity savedCard = new CreditCardEntity();
        ReflectionTestUtils.setField(savedCard, "id", 200L);
        when(cardRepository.save(createdCard)).thenReturn(savedCard);
        when(cardHelper.buildCreditCardResponse(savedCard))
                .thenReturn(mock(CreditCardResponse.class));

        CreditCardResponse response = cardService.createCard(request);

        assertNotNull(response);
        verify(defaultCardFactory).createCard(user, cardType, request);
        verify(cardRepository).save(createdCard);
    }

    @Test
    void testCreateCard_UserNotFound() {
        CreateCardRequest request = mock(CreateCardRequest.class);
        when(request.getUserId()).thenReturn(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> cardService.createCard(request));
    }

    @Test
    void testUpdateCardStatus_Success_StatusChanged() {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", 1L);

        CreditCardEntity card = new CreditCardEntity();
        ReflectionTestUtils.setField(card, "id", 300L);
        card.setUser(user);
        card.setCardStatus(CardStatus.ACTIVE);

        UpdateCardStatusRequest request = new UpdateCardStatusRequest();
        request.setCardStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(300L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(CreditCardEntity.class))).thenReturn(card);
        when(cardHelper.buildCreditCardResponse(card))
                .thenReturn(mock(CreditCardResponse.class));

        CreditCardResponse response = cardService.updateCardStatus(request, 300L);

        assertNotNull(response);
        assertEquals(CardStatus.BLOCKED, card.getCardStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void testUpdateCardStatus_Idempotent_NoChange() {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", 1L);

        CreditCardEntity card = new CreditCardEntity();
        ReflectionTestUtils.setField(card, "id", 301L);
        card.setUser(user);
        card.setCardStatus(CardStatus.BLOCKED);

        UpdateCardStatusRequest request = new UpdateCardStatusRequest();
        request.setCardStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(301L)).thenReturn(Optional.of(card));
        when(cardHelper.buildCreditCardResponse(card))
                .thenReturn(mock(CreditCardResponse.class));

        CreditCardResponse response = cardService.updateCardStatus(request, 301L);

        assertNotNull(response);
        assertEquals(CardStatus.BLOCKED, card.getCardStatus());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void testUpdateCardStatus_NotAllowed() {
        UserEntity otherUser = new UserEntity();
        ReflectionTestUtils.setField(otherUser, "id", 2L);

        CreditCardEntity card = new CreditCardEntity();
        ReflectionTestUtils.setField(card, "id", 304L);
        card.setUser(otherUser);
        card.setCardStatus(CardStatus.BLOCKED);

        UpdateCardStatusRequest request = new UpdateCardStatusRequest();
        request.setCardStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(304L)).thenReturn(Optional.of(card));

        assertThrows(UnauthorizedApplicationActionException.class,
                () -> cardService.updateCardStatus(request, 304L));
    }

    @Test
    void testUpdateCardStatus_CardNotFound() {
        UpdateCardStatusRequest request = new UpdateCardStatusRequest();
        request.setCardStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(123L)).thenReturn(Optional.empty());

        assertThrows(CreditCardNotFoundException.class,
                () -> cardService.updateCardStatus(request, 123L));
    }

    @Test
    void testGetAllCardsByUserId_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,
                () -> cardService.getAllCardsByUserId());
    }

    @Test
    void testCreateCard_CardTypeNotFound() {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", 1L);

        CreateCardRequest request = mock(CreateCardRequest.class);
        when(request.getUserId()).thenReturn(1L);
        when(request.getCardTypeId()).thenReturn(99L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardTypeNotFoundException.class,
                () -> cardService.createCard(request));
    }

    @Test
    void testGetCardTypes_Success() {
        CardTypeEntity type1 = new CardTypeEntity();
        CardTypeEntity type2 = new CardTypeEntity();

        when(cardTypeRepository.findAll()).thenReturn(List.of(type1, type2));

        List<CardTypeEntity> types = cardService.getCardTypes();

        assertEquals(2, types.size());
        verify(cardTypeRepository).findAll();
    }

}
