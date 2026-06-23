package com.aimr.notify.domain.enums;

import lombok.Getter;

@Getter
 public enum NotificationStatus {
    FAILED("FAILED"),
     PENDING_RETRY("PENDING_RETRY"),
    DELIVERED("DELIVERED"),
    IN_FLIGHT("IN_FLIGHT"),
    AWAITING_QUEUE("AWAITING_QUEUE"),
    QUEUED("QUEUED");

     private final String value;

    NotificationStatus(final String value){
        this.value=value;
    }
}