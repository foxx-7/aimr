package com.aimr.notify.models.dto;

import com.aimr.notify.models.enums.NotificationChannel;
import com.aimr.notify.models.enums.SummaryWindow;

import java.time.Instant;
import java.util.Map;

public record NotificationSummary(
        SummaryWindow window,
        Instant from,
        Instant to,
        Map<NotificationChannel, ChannelSummary> byChannel,
        long grandTotalSent,
        double overallDeliveryRate
) {}