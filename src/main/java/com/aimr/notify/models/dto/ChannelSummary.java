package com.aimr.notify.models.dto;

import com.aimr.notify.models.enums.NotificationChannel;

public record ChannelSummary(
        NotificationChannel channel,
        long totalSent,
        long totalDelivered,
        long totalFailed,
        long totalPending,
        double deliveryRate
) {}