package com.aimr.notify.api.dto.request;

import com.aimr.notify.domain.enums.NotificationChannel;

public record UpdateSenderIdentityRequest(
    String senderName,
    String senderAddress,
    NotificationChannel channel
) {
}