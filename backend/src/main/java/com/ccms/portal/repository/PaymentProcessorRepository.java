package com.ccms.portal.repository;

import com.ccms.portal.entity.PaymentProcessor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentProcessorRepository extends JpaRepository<PaymentProcessor, Long> {

  List<PaymentProcessor> findByNameContainingIgnoreCase(String name);
}
