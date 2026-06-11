package com.aimr.notify.model.enums;

import lombok.Getter;

@Getter
public enum Role {

    SUPER_ADMIN("SUPER_ADMIN"),
    OWNER("OWNER"),
   ADMIN("ADMIN"),
    MEMBER("MEMBER");

    private final String value;

   Role(final String value){
       this.value=value;
   }
}

