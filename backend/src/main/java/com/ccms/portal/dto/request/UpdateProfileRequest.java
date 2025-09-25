package com.ccms.portal.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class UpdateProfileRequest {
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private Double annualIncome;
}
