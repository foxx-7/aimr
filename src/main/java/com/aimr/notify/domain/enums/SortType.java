package com.aimr.notify.domain.enums;

import lombok.Getter;

@Getter
public enum SortType {

    ASC("ASC"),
    DESC("DESC");

    private final String value;

    SortType(final String value) {
        this.value = value;
    }
}
