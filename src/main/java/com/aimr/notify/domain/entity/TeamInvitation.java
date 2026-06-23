package com.aimr.notify.domain.entity;

import com.aimr.notify.domain.enums.InvitationStatus;
import com.aimr.notify.domain.enums.Role;
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
    name = "invitations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"email", "tenant_id"}),
    indexes = {
        @Index(name = "idx_invitation_tenant_id", columnList = "tenant_id")
    }
)
public class TeamInvitation {

    @Id
    private String id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "token", unique = true)
    private String token;

    @Column(name = "invited_by")
    private String invitedByUserId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

    @CreationTimestamp
    private Instant invitedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
