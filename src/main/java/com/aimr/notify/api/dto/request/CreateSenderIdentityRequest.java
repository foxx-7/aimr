package com.aimr.notify.api.dto.request;

import com.aimr.notify.domain.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSenderIdentityRequest(
    @NotBlank String name,
    @NotBlank String address,
    @NotNull NotificationChannel channel
) {
}