package com.aimr.notify.service.interfaces;


import com.aimr.notify.aop.ValidateTenant;
import com.aimr.notify.models.dto.NotificationSummary;
import com.aimr.notify.models.entity.IdempotencyKey;
import com.aimr.notify.models.enums.NotificationChannel;
import com.aimr.notify.models.enums.SummaryWindow;
import com.aimr.notify.models.dto.request.SendNotificationRequest;
import com.aimr.notify.models.dto.response.NotificationSearchResponse;
import com.aimr.notify.models.enums.NotificationStatus;

import java.time.Instant;

public interface NotificationService {
    String sendNotification(SendNotificationRequest request);

    /*
        notification browsing should be status specific-> EMAIL, SMS, PUSH
        like browse -> FAILED, SUCCESS, PENDING
     */
    @ValidateTenant
    NotificationSearchResponse browseNotification(Instant anchorTime, NotificationChannel channel, NotificationStatus status, String cursor);

    @ValidateTenant
    NotificationSummary getNotificationSummary(SummaryWindow window);

    void markNotificationStatus(String tenantId, String id, String providerMessageId, NotificationStatus status);

    IdempotencyKey getIdempotencyKey(String tenantId, String requestId);
}
