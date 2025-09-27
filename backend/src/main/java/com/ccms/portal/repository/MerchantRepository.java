package com.ccms.portal.repository;

import com.ccms.portal.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

  List<Merchant> findByIsActiveTrue();

  List<Merchant> findByCategory(String category);

  List<Merchant> findByNameContainingIgnoreCase(String name);
}