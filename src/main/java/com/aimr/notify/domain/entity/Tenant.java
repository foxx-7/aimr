package com.aimr.notify.domain.entity;

import com.aimr.notify.domain.entity.annotations.CachePrefix;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "tenants",
    indexes = {
        @Index(name = "idx_tenant_id_owner", columnList = "id, owner_id")
    }
)
@CachePrefix("TENANT.")
public class Tenant {

    @Id
    private String id;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
