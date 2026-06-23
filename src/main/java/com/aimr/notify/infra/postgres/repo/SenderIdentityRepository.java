package com.aimr.notify.infra.postgres.repo;

import com.aimr.notify.domain.entity.SenderIdentity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SenderIdentityRepository extends JpaRepository<@NonNull SenderIdentity, @NonNull String> {
    Optional<SenderIdentity> findByTenantIdAndId(String tenantId, String id);
    boolean existsByTenantIdAndSenderName(String tenantId, String name);
    List<SenderIdentity> findAllByTenantId(String tenantId);
}
