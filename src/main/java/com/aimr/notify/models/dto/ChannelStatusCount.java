package com.aimr.notify.models.dto;

import com.aimr.notify.models.enums.NotificationChannel;
import com.aimr.notify.models.enums.NotificationStatus;

public record ChannelStatusCount(NotificationChannel channel, NotificationStatus status, long count) {}