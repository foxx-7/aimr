package com.aimr.notify.model.enums;

import lombok.Getter;

@Getter
public enum InvitationStatus {
    PENDING("PENDING"),
    ACCEPTED("ACCEPTED"),
    EXPIRED("EXPIRED");

   private final String value;

   InvitationStatus(final String value){
       this.value=value;
   }
}
