package com.ccms.portal.service;

import com.ccms.portal.dto.response.MerchantResponse;
import com.ccms.portal.entity.Merchant;
import com.ccms.portal.repository.MerchantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    @InjectMocks
    private MerchantService merchantService;

    private Merchant merchant1;
    private Merchant merchant2;

    @BeforeEach
    void setUp() {
        merchant1 = new Merchant();
        merchant1.setId(1L);
        merchant1.setName("Amazon");
        merchant1.setMerchantIdentifier("AMZ-001");
        merchant1.setCategory("E-Commerce");
        merchant1.setCreatedAt(LocalDateTime.of(2024, 1, 10, 15, 30));

        merchant2 = new Merchant();
        merchant2.setId(2L);
        merchant2.setName("Flipkart");
        merchant2.setMerchantIdentifier("FLP-002");
        merchant2.setCategory("E-Commerce");
        // Leave createdAt null to cover the null-path in mapToMerchantResponse
        merchant2.setCreatedAt(null);
    }

    /** -------------------- getAllMerchants() -------------------- */

    @Test
    void getAllMerchants_success() {
        when(merchantRepository.findAllByOrderByNameAsc())
                .thenReturn(Arrays.asList(merchant1, merchant2));

        List<MerchantResponse> responses = merchantService.getAllMerchants();

        assertEquals(2, responses.size());
        assertEquals("Amazon", responses.get(0).getName());
        assertNotNull(responses.get(0).getCreatedAt());
        // second merchant had null createdAt, should be null in response
        assertNull(responses.get(1).getCreatedAt());

        verify(merchantRepository, times(1)).findAllByOrderByNameAsc();
    }

    @Test
    void getAllMerchants_emptyList() {
        when(merchantRepository.findAllByOrderByNameAsc())
                .thenReturn(Collections.emptyList());

        List<MerchantResponse> responses = merchantService.getAllMerchants();

        assertTrue(responses.isEmpty());
        verify(merchantRepository).findAllByOrderByNameAsc();
    }

    /** -------------------- getMerchantsByCategory() -------------------- */

    @Test
    void getMerchantsByCategory_success() {
        when(merchantRepository.findByCategoryOrderByNameAsc("E-Commerce"))
                .thenReturn(Arrays.asList(merchant1, merchant2));

        List<MerchantResponse> responses = merchantService.getMerchantsByCategory("E-Commerce");

        assertEquals(2, responses.size());
        assertEquals("AMZ-001", responses.get(0).getMerchantIdentifier());
        verify(merchantRepository, times(1))
                .findByCategoryOrderByNameAsc("E-Commerce");
    }

    @Test
    void getMerchantsByCategory_empty() {
        when(merchantRepository.findByCategoryOrderByNameAsc("Toys"))
                .thenReturn(Collections.emptyList());

        List<MerchantResponse> responses = merchantService.getMerchantsByCategory("Toys");

        assertTrue(responses.isEmpty());
        verify(merchantRepository).findByCategoryOrderByNameAsc("Toys");
    }

    /** -------------------- searchMerchants() -------------------- */

    @Test
    void searchMerchants_success() {
        when(merchantRepository.findByNameContainingIgnoreCaseOrderByNameAsc("Ama"))
                .thenReturn(List.of(merchant1));

        List<MerchantResponse> responses = merchantService.searchMerchants("Ama");

        assertEquals(1, responses.size());
        assertEquals("Amazon", responses.get(0).getName());
        verify(merchantRepository).findByNameContainingIgnoreCaseOrderByNameAsc("Ama");
    }

    @Test
    void searchMerchants_noMatch() {
        when(merchantRepository.findByNameContainingIgnoreCaseOrderByNameAsc("XYZ"))
                .thenReturn(Collections.emptyList());

        List<MerchantResponse> responses = merchantService.searchMerchants("XYZ");

        assertTrue(responses.isEmpty());
        verify(merchantRepository)
                .findByNameContainingIgnoreCaseOrderByNameAsc("XYZ");
    }
}
