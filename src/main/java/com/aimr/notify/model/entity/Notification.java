package com.aimr.notify.model.entity;

import com.aimr.notify.model.enums.NotificationChannel;
import com.aimr.notify.model.interfaces.TenantAware;
import com.aimr.notify.model.entity.annotations.CachePrefix;
import com.aimr.notify.model.enums.NotificationStatus;
import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;


@Data
@Document("notifications")
@CachePrefix("NOTIFICATION.")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@CompoundIndexes({
        @CompoundIndex(
                name = "tenant_status_time_idx",
                def = "{'tenantId': 1, 'status': 1, 'submittedAt': -1, '_id': -1}"
        )}
)
public class Notification extends AbstractEntity implements TenantAware {

    private String id;
    private String tenantId;
    private String requestId;
    private String traceId;
    private String templateId;
    private String mailId;
    private NotificationChannel dispatchChannel;
    private NotificationStatus status;
    private Map<String, String> variables;
    private Instant submittedAt;
}
