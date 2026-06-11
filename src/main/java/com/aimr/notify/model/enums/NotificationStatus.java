package com.aimr.notify.model.enums;

import lombok.Getter;

@Getter
 public enum NotificationStatus {
    FAILED("FAILED"),
     PENDING("PENDING"),
    DELIVERED("DELIVERED"),
    IN_FLIGHT("IN_FLIGHT");

     private final String value;

    NotificationStatus(final String value){
        this.value=value;
    }
}