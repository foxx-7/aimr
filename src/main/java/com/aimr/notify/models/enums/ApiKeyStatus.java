package com.aimr.notify.models.enums;

import lombok.Getter;

@Getter
public enum ApiKeyStatus {
    ACTIVE("ACTIVE"),
    REVOKED("REVOKED");

    public final String value;

    ApiKeyStatus(final String value){
        this.value=value;
    }
}
