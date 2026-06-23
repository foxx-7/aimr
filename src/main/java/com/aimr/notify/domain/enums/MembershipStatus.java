package com.aimr.notify.domain.enums;

import lombok.Getter;

@Getter
public enum MembershipStatus {
    ACTIVE("ACTIVE"),
    INVITED("INVITED"),
    SUSPENDED("SUSPENDED");

    private final String value;

    MembershipStatus(final String value){
        this.value=value;
    }
}
