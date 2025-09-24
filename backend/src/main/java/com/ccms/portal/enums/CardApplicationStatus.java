package com.ccms.portal.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CardApplicationStatus {
    Accepted("Accepted"),
    Rejected("Rejected"),
    Pending("Pending");

    private final String applicationStatus;
    CardApplicationStatus(String applicationStatus){
        this.applicationStatus = applicationStatus;
    }

}