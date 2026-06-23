package com.aimr.notify.api.dto.response;

import com.aimr.notify.domain.enums.NotificationChannel;
import com.aimr.notify.domain.enums.SummaryWindow;

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