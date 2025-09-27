package com.ccms.portal.repository;

import com.ccms.portal.entity.MerchantAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MerchantAccountRepository extends JpaRepository<MerchantAccount, Long> {

  List<MerchantAccount> findByMerchantId(Long merchantId);

  List<MerchantAccount> findByIsActiveTrue();

  List<MerchantAccount> findByNameContainingIgnoreCase(String name);
}
