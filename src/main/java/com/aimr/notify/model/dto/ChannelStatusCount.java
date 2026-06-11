package com.aimr.notify.model.dto;

import com.aimr.notify.model.enums.NotificationChannel;
import com.aimr.notify.model.enums.NotificationStatus;

public record ChannelStatusCount(NotificationChannel channel, NotificationStatus status, long count) {}