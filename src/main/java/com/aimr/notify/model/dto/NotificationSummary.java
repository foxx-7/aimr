package com.aimr.notify.model.dto;

import com.aimr.notify.model.enums.NotificationChannel;
import com.aimr.notify.model.enums.SummaryWindow;

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