package com.ccms.portal.service;

import com.ccms.portal.dto.request.CreateCardRequest;
import com.ccms.portal.dto.request.UpdateCardStatusRequest;
import com.ccms.portal.dto.response.CreditCardResponse;
import com.ccms.portal.entity.CardTypeEntity;
import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.entity.UserEntity;
import com.ccms.portal.enums.CardStatus;
import com.ccms.portal.enums.NetworkType;
import com.ccms.portal.exception.CardTypeNotFoundException;
import com.ccms.portal.exception.CreditCardNotFoundException;
import com.ccms.portal.exception.UnauthorizedApplicationActionException;
import com.ccms.portal.exception.UserNotFoundException;
import com.ccms.portal.repository.CardTypeRepository;
import com.ccms.portal.repository.CreditCardRepository;
import com.ccms.portal.repository.UserRepository;
import com.ccms.portal.util.CreditCardUtil;
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

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CreditCardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardTypeRepository cardTypeRepository;

    @Mock
    private CreditCardUtil cardHelper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CardService cardService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetAllCardsByUserId() {
        // Create test data using mocks
        JwtUserDetails userDetails = mock(JwtUserDetails.class);
        when(userDetails.getUserId()).thenReturn(1L);

        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(1L);

        CreditCardEntity cardEntity = mock(CreditCardEntity.class);
        when(cardEntity.getId()).thenReturn(1L);

        CreditCardResponse cardResponse = CreditCardResponse.builder()
                .id(1L)
                .cardHolderName("Test User")
                .build();

        List<CreditCardEntity> cardEntities = new ArrayList<>();
        cardEntities.add(cardEntity);

        // Mock security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.findAllByUserIdWithCardType(1L)).thenReturn(cardEntities);
        when(cardHelper.buildCreditCardResponse(any())).thenReturn(cardResponse);

        // Call method
        List<CreditCardResponse> result = cardService.getAllCardsByUserId();

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findById(1L);
        verify(cardRepository).findAllByUserIdWithCardType(1L);
    }

    @Test
    void testGetAllCardsByUserId_UserNotFound() {
        // Create test data using mocks
        JwtUserDetails userDetails = mock(JwtUserDetails.class);
        when(userDetails.getUserId()).thenReturn(999L);

        // Mock security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Test exception
        assertThrows(UserNotFoundException.class, () -> {
            cardService.getAllCardsByUserId();
        });
    }

    @Test
    void testCreateCard() {
        // Create test data
        CreateCardRequest request = new CreateCardRequest();
        request.setUserId(1L);
        request.setCardTypeId(1L);
        request.setCreditLimit(5000.0);

        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(1L);

        CardTypeEntity cardType = mock(CardTypeEntity.class);
        when(cardType.getId()).thenReturn(1L);
        when(cardType.getName()).thenReturn("Gold Card");

        CreditCardEntity savedEntity = mock(CreditCardEntity.class);
        when(savedEntity.getId()).thenReturn(1L);

        CreditCardResponse response = CreditCardResponse.builder()
                .id(1L)
                .build();

        // Mock repositories
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardTypeRepository.findById(1L)).thenReturn(Optional.of(cardType));
        when(cardHelper.generateCardNumber()).thenReturn("1234567890123456");
        when(cardHelper.generateCvv()).thenReturn(123);
        when(cardHelper.generateExpiryDate(5)).thenReturn(Date.valueOf(LocalDate.now().plusYears(5)));
        when(cardRepository.save(any())).thenReturn(savedEntity);
        when(cardHelper.buildCreditCardResponse(any())).thenReturn(response);

        // Call method
        CreditCardResponse result = cardService.createCard(request);

        // Verify
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(cardTypeRepository).findById(1L);
        verify(cardRepository).save(any());
    }

    @Test
    void testCreateCard_UserNotFound() {
        // Create test data
        CreateCardRequest request = new CreateCardRequest();
        request.setUserId(999L);

        // Mock repository
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Test exception
        assertThrows(UserNotFoundException.class, () -> {
            cardService.createCard(request);
        });
    }

    @Test
    void testUpdateCardStatus() {
        // Create test data using mocks
        JwtUserDetails userDetails = mock(JwtUserDetails.class);
        when(userDetails.getUserId()).thenReturn(1L);

        UpdateCardStatusRequest request = new UpdateCardStatusRequest();
        request.setCardStatus(CardStatus.BLOCKED);

        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(1L);

        CreditCardEntity cardEntity = mock(CreditCardEntity.class);
        when(cardEntity.getId()).thenReturn(1L);
        when(cardEntity.getUser()).thenReturn(user);

        CreditCardEntity savedEntity = mock(CreditCardEntity.class);
        when(savedEntity.getId()).thenReturn(1L);
        when(savedEntity.getCardStatus()).thenReturn(CardStatus.BLOCKED);

        CreditCardResponse response = CreditCardResponse.builder()
                .id(1L)
                .cardStatus(CardStatus.BLOCKED)
                .build();

        // Mock security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(cardEntity));
        when(cardRepository.save(any())).thenReturn(savedEntity);
        when(cardHelper.buildCreditCardResponse(any())).thenReturn(response);

        // Call method
        CreditCardResponse result = cardService.updateCardStatus(request, 1L);

        // Verify
        assertNotNull(result);
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(any());
    }
} 