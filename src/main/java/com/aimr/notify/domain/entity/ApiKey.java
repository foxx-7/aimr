package com.aimr.notify.domain.entity;

import com.aimr.notify.domain.enums.ApiKeyStatus;
import com.aimr.notify.domain.aop.TenantAware;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "api_keys",
    indexes = {
        @Index(name = "idx_api_key_tenant_id", columnList = "tenant_id")
    }
)
public class ApiKey implements TenantAware {

    @Id
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "name")
    private String name;

    @Column(name = "key_prefix")
    private String keyPrefix;

    @Column(name = "hashed_key", nullable = false, unique = true)
    private String hashedKey;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ApiKeyStatus status;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
