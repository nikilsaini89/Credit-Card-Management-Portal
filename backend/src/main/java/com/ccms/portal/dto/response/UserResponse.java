package com.ccms.portal.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private Boolean isEligibleForBNPL;


}
