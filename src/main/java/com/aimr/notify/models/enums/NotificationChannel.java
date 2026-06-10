package com.aimr.notify.models.enums;

import lombok.Getter;

@Getter
public enum NotificationChannel {

    EMAIL("EMAIL"),
    SMS("SMS"),
    PUSH("PUSH"),
    WEBHOOK("WEBHOOK");

    private final String value;

    NotificationChannel(final String value){
        this.value=value;
    }
}
