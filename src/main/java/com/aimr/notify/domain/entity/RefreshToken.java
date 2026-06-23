package com.aimr.notify.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "refresh_token", indexes = {
    @Index(name = "idx_refresh_token_user_id", columnList = "user_id"),
    @Index(name = "idx_refresh_token_hash",    columnList = "token_hash")
})
public class RefreshToken {
    @Id
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "token_hash")
    private String tokenHash;

    @Column(name = "is_revoked")
    private boolean revoked;

    @CreationTimestamp
    private Instant createdAt;
}
