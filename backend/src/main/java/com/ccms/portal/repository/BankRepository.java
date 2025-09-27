package com.ccms.portal.repository;

import com.ccms.portal.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {

  List<Bank> findByNameContainingIgnoreCase(String name);

  List<Bank> findByCountry(String country);

  List<Bank> findBySwiftCode(String swiftCode);
}
