package com.ccms.portal.controller;

import com.ccms.portal.entity.Bank;
import com.ccms.portal.service.BankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banks")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BankController {

    private final BankService bankService;

    @GetMapping
    public ResponseEntity<List<Bank>> getAllBanks() {
        log.info("GET /api/banks - Fetching all banks");
        try {
            List<Bank> banks = bankService.getAllBanks();
            log.info("Successfully fetched {} banks", banks.size());
            return ResponseEntity.ok(banks);
        } catch (Exception e) {
            log.error("Error fetching banks: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bank> getBankById(@PathVariable Long id) {
        log.info("GET /api/banks/{} - Fetching bank by ID", id);
        try {
            Bank bank = bankService.getBankById(id);
            return ResponseEntity.ok(bank);
        } catch (RuntimeException e) {
            log.error("Bank not found with ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching bank with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Bank>> searchBanks(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String swiftCode) {
        log.info("GET /api/banks/search - Searching banks with name: {}, country: {}, swiftCode: {}", 
                name, country, swiftCode);
        try {
            List<Bank> banks;
            if (name != null && !name.trim().isEmpty()) {
                banks = bankService.getBanksByName(name);
            } else if (country != null && !country.trim().isEmpty()) {
                banks = bankService.getBanksByCountry(country);
            } else if (swiftCode != null && !swiftCode.trim().isEmpty()) {
                banks = List.of(bankService.getBankBySwiftCode(swiftCode));
            } else {
                banks = bankService.getAllBanks();
            }
            log.info("Found {} banks matching search criteria", banks.size());
            return ResponseEntity.ok(banks);
        } catch (RuntimeException e) {
            log.error("Bank not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error searching banks: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
