package com.aimr.notify.api.dto.response;

import com.aimr.notify.domain.enums.NotificationChannel;

public record ChannelSummary(
        NotificationChannel channel,
        long totalSent,
        long totalDelivered,
        long totalFailed,
        long totalPending,
        double deliveryRate
) {}