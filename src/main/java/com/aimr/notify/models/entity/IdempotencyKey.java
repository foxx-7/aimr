package com.aimr.notify.models.entity;

import com.aimr.notify.models.enums.NotificationStatus;
import com.aimr.notify.models.entity.annotations.CachePrefix;
import jakarta.persistence.Column;
import lombok.Data;

@Data
@CachePrefix(value = "IDEM.", cacheTTL = 60)
public class IdempotencyKey {

    @Column(name = "key",nullable = false)
    private String key;

    @Column(name = "status")
    private NotificationStatus notificationStatus;

    public IdempotencyKey(String mailId, NotificationStatus notificationStatus){
        setKey(mailId);
        setNotificationStatus(notificationStatus);
    }
}
