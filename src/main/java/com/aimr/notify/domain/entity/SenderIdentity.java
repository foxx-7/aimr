package com.aimr.notify.domain.entity;

import com.aimr.notify.domain.enums.NotificationChannel;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "sender_identity")
public class SenderIdentity {
    @Id
    private String id;

    private String tenantId;
    private String senderAddress;
    private String senderName;
    private NotificationChannel channel;
    private boolean isVerified;
    private boolean isDefault;

    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}
