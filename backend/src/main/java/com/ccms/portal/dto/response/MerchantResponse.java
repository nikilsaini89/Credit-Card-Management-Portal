package com.ccms.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantResponse {

  private Long id;
  private String name;
  private String category;
  private String description;
  private String logoUrl;
  private boolean isActive;
}
