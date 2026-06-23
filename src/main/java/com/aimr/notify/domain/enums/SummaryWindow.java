package com.aimr.notify.domain.enums;

import lombok.Getter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Getter
public enum SummaryWindow {

    LAST_1_HOUR,
    LAST_24_HOURS,
    LAST_7_DAYS;

    public Instant toInstant(){
        return switch (this){
            case LAST_1_HOUR -> Instant.now().minus(1, ChronoUnit.HOURS);
            case LAST_24_HOURS -> Instant.now().minus(24,ChronoUnit.HOURS);
            case LAST_7_DAYS -> Instant.now().minus(7,ChronoUnit.HOURS);
        };
    }

}
