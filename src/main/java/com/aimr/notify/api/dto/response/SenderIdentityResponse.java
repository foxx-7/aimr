package com.aimr.notify.api.dto.response;

import com.aimr.notify.domain.entity.SenderIdentity;
import java.time.Instant;

public record SenderIdentityResponse(
    String name,
    String address,
    Instant createdAt,
    Instant updatedAt
) {
    public static SenderIdentityResponse from(SenderIdentity senderIdentity) {
        return new SenderIdentityResponse(
                senderIdentity.getSenderName(),
                senderIdentity.getSenderAddress(),
                senderIdentity.getCreatedAt(),
                senderIdentity.getUpdatedAt());
    }
}