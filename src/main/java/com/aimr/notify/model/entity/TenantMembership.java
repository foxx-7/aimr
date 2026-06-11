package com.aimr.notify.model.entity;

import com.aimr.notify.model.entity.annotations.CachePrefix;
import com.aimr.notify.model.enums.MembershipStatus;
import com.aimr.notify.model.enums.Role;
import com.aimr.notify.model.interfaces.TenantAware;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "tenant_membership",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tenant_id"})
)
@Entity
@CachePrefix(value = "TENANT_MEMBERSHIP.")
public class TenantMembership implements TenantAware {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private MembershipStatus status;

    @Column(name = "invited_by")
    private String invitedByUserId;

    @CreationTimestamp
    private Instant joinedAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
