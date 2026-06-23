package com.aimr.notify.domain.enums;

import lombok.Getter;

@Getter
public enum BatchStatus {
    AWAITING_QUEUE("AWAITING_QUEUE"),
    QUEUED("QUEUED"),
    PENDING("PENDING");

    private final String value;

    BatchStatus(final String value){
        this.value=value;
    }
}
