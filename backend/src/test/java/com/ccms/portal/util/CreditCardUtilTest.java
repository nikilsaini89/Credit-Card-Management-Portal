package com.ccms.portal.util;

import com.ccms.portal.dto.response.CreditCardResponse;
import com.ccms.portal.entity.CardTypeEntity;
import com.ccms.portal.entity.CreditCardEntity;
import com.ccms.portal.entity.UserEntity;
import com.ccms.portal.enums.CardStatus;
import com.ccms.portal.enums.NetworkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditCardUtilTest {

    @InjectMocks
    private CreditCardUtil creditCardUtil;

    @Test
    void testGenerateCardNumber() {
        // Call method
        String cardNumber = creditCardUtil.generateCardNumber();

        // Verify
        assertNotNull(cardNumber);
        assertEquals(16, cardNumber.length());
        assertTrue(cardNumber.matches("\\d{16}"));
    }

    @Test
    void testGenerateCvv() {
        // Call method
        Integer cvv = creditCardUtil.generateCvv();

        // Verify
        assertNotNull(cvv);
        assertTrue(cvv >= 100 && cvv <= 999);
    }

    @Test
    void testGenerateExpiryDate() {
        // Call method
        Date expiryDate = creditCardUtil.generateExpiryDate(5);

        // Verify
        assertNotNull(expiryDate);
        LocalDate expectedDate = LocalDate.now().plusYears(5);
        assertEquals(Date.valueOf(expectedDate), expiryDate);
    }

    @Test
    void testBuildCreditCardResponse() {
        // Create test data using mocks since entities don't have setters
        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(1L);
        when(user.getName()).thenReturn("John Doe");

        CardTypeEntity cardType = mock(CardTypeEntity.class);
        when(cardType.getId()).thenReturn(1L);
        when(cardType.getName()).thenReturn("Gold Card");
        when(cardType.getNetworkType()).thenReturn(NetworkType.VISA);
        when(cardType.getDescription()).thenReturn("Premium Card");

        CreditCardEntity cardEntity = mock(CreditCardEntity.class);
        when(cardEntity.getId()).thenReturn(1L);
        when(cardEntity.getUser()).thenReturn(user);
        when(cardEntity.getCardType()).thenReturn(cardType);
        when(cardEntity.getCardNumber()).thenReturn("1234567890123456");
        when(cardEntity.getCardStatus()).thenReturn(CardStatus.ACTIVE);
        when(cardEntity.getCreditLimit()).thenReturn(5000.0);
        when(cardEntity.getAvailableLimit()).thenReturn(4500.0);
        when(cardEntity.getExpiryDate()).thenReturn(Date.valueOf(LocalDate.now().plusYears(5)));
        when(cardEntity.getCvv()).thenReturn(123);

        // Call method
        CreditCardResponse response = creditCardUtil.buildCreditCardResponse(cardEntity);

        // Verify
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getCardHolderName());
        assertEquals("1234567890123456", response.getCardNumber());
        assertEquals(CardStatus.ACTIVE, response.getCardStatus());
        assertEquals(5000.0, response.getCreditLimit());
        assertEquals(4500.0, response.getAvailableLimit());
        assertEquals(123, response.getCvv());
        assertNotNull(response.getCardType());
        assertEquals("Gold Card", response.getCardType().getName());
        assertEquals(NetworkType.VISA, response.getCardType().getNetworkType());
        assertEquals("Premium Card", response.getCardType().getDescription());
    }

    @Test
    void testGenerateCardNumber_UniqueNumbers() {
        // Generate multiple card numbers
        String cardNumber1 = creditCardUtil.generateCardNumber();
        String cardNumber2 = creditCardUtil.generateCardNumber();

        // Verify they are different (very high probability)
        assertNotEquals(cardNumber1, cardNumber2);
    }
} 