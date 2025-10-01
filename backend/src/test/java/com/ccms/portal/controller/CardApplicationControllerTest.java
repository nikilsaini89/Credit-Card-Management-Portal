package com.ccms.portal.controller;

import com.ccms.portal.dto.request.CardApplicationRequest;
import com.ccms.portal.dto.request.UpdateStatusRequest;
import com.ccms.portal.dto.response.CardApplicationResponse;
import com.ccms.portal.enums.CardApplicationStatus;
import com.ccms.portal.service.CardApplicationService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardApplicationControllerTest {

    @Mock
    private CardApplicationService cardApplicationService;

    @InjectMocks
    private CardApplicationController cardApplicationController;

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /** -------------------  POST  --------------------- */

    @Test
    void testApplyForCard_Success() {
        CardApplicationRequest request = new CardApplicationRequest();
        request.setCardTypeId(10L);
        request.setRequestLimit(5000L);

        CardApplicationResponse saved = new CardApplicationResponse();
        saved.setId(1L);
        saved.setRequestedLimit(5000L);
        saved.setStatus(CardApplicationStatus.PENDING.name());

        when(cardApplicationService.apply(any(CardApplicationRequest.class)))
                .thenReturn(saved);

        ResponseEntity<?> response = cardApplicationController.applyForCard(request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof CardApplicationResponse);
        CardApplicationResponse body = (CardApplicationResponse) response.getBody();
        assertEquals(1L, body.getId());
        assertEquals(5000L, body.getRequestedLimit());
        assertEquals("PENDING", body.getStatus());
    }

    @Test
    void testApplyForCard_NullBody() {
        // should still reach logger and throw NullPointer when service called
        when(cardApplicationService.apply(null)).thenThrow(new NullPointerException("Null body"));

        assertThrows(NullPointerException.class,
                () -> cardApplicationController.applyForCard(null));
    }

    @Test
    void testApplyForCard_ServiceThrows() {
        CardApplicationRequest request = new CardApplicationRequest();
        request.setCardTypeId(5L);
        request.setRequestLimit(2000L);

        when(cardApplicationService.apply(any())).thenThrow(new RuntimeException("Service failure"));

        assertThrows(RuntimeException.class,
                () -> cardApplicationController.applyForCard(request));
    }

    @Test
    void testApplyForCard_InvalidRequest() {
        // missing required fields -> should fail validation
        CardApplicationRequest request = new CardApplicationRequest();
        Set<ConstraintViolation<CardApplicationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Expected validation to fail for missing fields");
    }

    /** -------------------  GET  --------------------- */

    @Test
    void testGetApplications_WithData() {
        CardApplicationResponse r1 = new CardApplicationResponse();
        r1.setId(1L);
        r1.setStatus(CardApplicationStatus.PENDING.name());

        CardApplicationResponse r2 = new CardApplicationResponse();
        r2.setId(2L);
        r2.setStatus(CardApplicationStatus.ACCEPTED.name());

        List<CardApplicationResponse> list = Arrays.asList(r1, r2);

        when(cardApplicationService.getApplications()).thenReturn(list);

        ResponseEntity<?> response = cardApplicationController.getApplications();

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        List<?> body = (List<?>) response.getBody();
        assertEquals(2, body.size());
    }

    @Test
    void testGetApplications_EmptyList() {
        when(cardApplicationService.getApplications()).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = cardApplicationController.getApplications();

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        List<?> body = (List<?>) response.getBody();
        assertTrue(body.isEmpty());
    }

    /** -------------------  PATCH  --------------------- */

    @Test
    void testUpdateApplication_Success() {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(CardApplicationStatus.ACCEPTED);

        CardApplicationResponse updated = new CardApplicationResponse();
        updated.setId(1L);
        updated.setStatus(CardApplicationStatus.ACCEPTED.name());

        when(cardApplicationService.updateStatus(eq(1L), eq(CardApplicationStatus.ACCEPTED)))
                .thenReturn(updated);

        ResponseEntity<?> response = cardApplicationController.updateApplication(1L, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        CardApplicationResponse body = (CardApplicationResponse) response.getBody();
        assertEquals("ACCEPTED", body.getStatus());
    }

    @Test
    void testUpdateApplication_OtherStatus() {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(CardApplicationStatus.REJECTED);

        CardApplicationResponse updated = new CardApplicationResponse();
        updated.setId(2L);
        updated.setStatus(CardApplicationStatus.REJECTED.name());

        when(cardApplicationService.updateStatus(eq(2L), eq(CardApplicationStatus.REJECTED)))
                .thenReturn(updated);

        ResponseEntity<?> response = cardApplicationController.updateApplication(2L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        CardApplicationResponse body = (CardApplicationResponse) response.getBody();
        assertEquals("REJECTED", body.getStatus());
    }

    @Test
    void testUpdateApplication_ServiceThrows() {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(CardApplicationStatus.REJECTED);

        when(cardApplicationService.updateStatus(eq(99L), eq(CardApplicationStatus.REJECTED)))
                .thenThrow(new RuntimeException("Application not found"));

        assertThrows(RuntimeException.class,
                () -> cardApplicationController.updateApplication(99L, request));
    }

    /** -------------------  DELETE  --------------------- */

    @Test
    void testDeleteApplication_Success() {
        CardApplicationResponse deleted = new CardApplicationResponse();
        deleted.setId(1L);

        when(cardApplicationService.deleteApplication(1L)).thenReturn(deleted);

        ResponseEntity<?> response = cardApplicationController.deleteApplication(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        CardApplicationResponse body = (CardApplicationResponse) response.getBody();
        assertEquals(1L, body.getId());
    }

    @Test
    void testDeleteApplication_ServiceReturnsNull() {
        when(cardApplicationService.deleteApplication(2L)).thenReturn(null);

        ResponseEntity<?> response = cardApplicationController.deleteApplication(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody(), "Expect null body if service returned null");
    }

    @Test
    void testDeleteApplication_ServiceThrows() {
        when(cardApplicationService.deleteApplication(99L))
                .thenThrow(new RuntimeException("Application not found"));

        assertThrows(RuntimeException.class,
                () -> cardApplicationController.deleteApplication(99L));
    }
}
