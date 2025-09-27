package com.ccms.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantAccountResponse {

  private Long id;
  private Long merchantId;
  private String name;
  private String accountNumber;
  private String bankName;
  private boolean isActive;
}
