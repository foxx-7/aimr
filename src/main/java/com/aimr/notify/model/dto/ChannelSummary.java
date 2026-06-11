package com.aimr.notify.model.dto;

import com.aimr.notify.model.enums.NotificationChannel;

public record ChannelSummary(
        NotificationChannel channel,
        long totalSent,
        long totalDelivered,
        long totalFailed,
        long totalPending,
        double deliveryRate
) {}