package com.aimr.notify.dao.interfaces;

import com.aimr.notify.models.dto.ChannelStatusCount;
import com.aimr.notify.models.dto.NotificationCursor;
import com.aimr.notify.models.enums.NotificationChannel;
import com.aimr.notify.models.entity.Notification;
import com.aimr.notify.models.enums.NotificationStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationDao {
    List<Notification> searchNotification(String tenantId,
                                          Instant anchorTime, NotificationCursor notificationCursor, NotificationStatus status, NotificationChannel channel);

    List<ChannelStatusCount> getStatusCountsByChannel(String tenantId, Instant from);

    void saveNotification(Notification notification);

    Optional<Notification> fetchNotificationByTenantIdAndRequestId(String tenantId, String requestId);

    List<Notification> fetchForExport(
            String tenantId, NotificationChannel channel,
            NotificationStatus status, Instant from, Instant to);
}
