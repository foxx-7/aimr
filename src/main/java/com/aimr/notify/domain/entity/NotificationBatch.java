package com.aimr.notify.domain.entity;

import com.aimr.notify.domain.enums.BatchStatus;
import com.aimr.notify.domain.enums.NotificationChannel;
import com.aimr.notify.domain.aop.TenantAware;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notification_batches", indexes = {
    @Index(name = "idx_batch_tenant_id", columnList = "tenant_id")
})
public class NotificationBatch extends AbstractEntity implements TenantAware {

    @Id
    private String id;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "channel")
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @Column(name = "template_id")
    private String templateId;

    @Column(name = "sender_identity")
    private String senderIdentity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dynamic_variables", columnDefinition = "jsonb")
    private Map<String, String> dynamicVariables;

    @Column(name = "target")
    private String target;

    @Column(name = "segment_id")
    private String segmentId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private BatchStatus status;

    @Column(name = "processed_count")
    private Long processedCount;

    @Column(name = "failed_count")
    private Long failedCount;
}
