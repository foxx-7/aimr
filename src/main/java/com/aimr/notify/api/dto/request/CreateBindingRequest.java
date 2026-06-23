package com.aimr.notify.api.dto.request;

import com.aimr.notify.domain.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;

import lombok.Builder;
import org.aspectj.bridge.IMessage;

@Builder
public record CreateBindingRequest(
    @NotBlank(message = "Name is mandatory") String name,
    @NotBlank(message = "Binding address is mandatory") String bindingAddress,
    @NotBlank(message = "channel is mandatory") NotificationChannel channel
) {
}