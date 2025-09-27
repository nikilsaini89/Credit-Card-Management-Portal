package com.ccms.portal.service;

import com.ccms.portal.entity.Bank;
import com.ccms.portal.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankService {

    private final BankRepository bankRepository;

    public List<Bank> getAllBanks() {
        log.info("Fetching all banks");
        return bankRepository.findAll();
    }

    public Bank getBankById(Long id) {
        log.info("Fetching bank with ID: {}", id);
        return bankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bank not found with ID: " + id));
    }

    public List<Bank> getBanksByName(String name) {
        log.info("Fetching banks by name: {}", name);
        return bankRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Bank> getBanksByCountry(String country) {
        log.info("Fetching banks by country: {}", country);
        return bankRepository.findByCountry(country);
    }

    public Bank getBankBySwiftCode(String swiftCode) {
        log.info("Fetching bank by SWIFT code: {}", swiftCode);
        List<Bank> banks = bankRepository.findBySwiftCode(swiftCode);
        if (banks.isEmpty()) {
            throw new RuntimeException("Bank not found with SWIFT code: " + swiftCode);
        }
        return banks.get(0);
    }
}
