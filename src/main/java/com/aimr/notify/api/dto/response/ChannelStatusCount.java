package com.aimr.notify.api.dto.response;

import com.aimr.notify.domain.enums.NotificationChannel;
import com.aimr.notify.domain.enums.NotificationStatus;

public record ChannelStatusCount(NotificationChannel channel, NotificationStatus status, long count) {}