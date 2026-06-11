package com.aimr.notify.service.interfaces;


import com.aimr.notify.aop.ValidateTenant;
import com.aimr.notify.model.dto.NotificationSummary;
import com.aimr.notify.model.entity.IdempotencyKey;
import com.aimr.notify.model.enums.NotificationChannel;
import com.aimr.notify.model.enums.SummaryWindow;
import com.aimr.notify.model.dto.request.SendNotificationRequest;
import com.aimr.notify.model.dto.response.NotificationSearchResponse;
import com.aimr.notify.model.enums.NotificationStatus;

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
