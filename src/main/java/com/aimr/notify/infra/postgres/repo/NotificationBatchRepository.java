package com.aimr.notify.infra.postgres.repo;

import com.aimr.notify.domain.entity.NotificationBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationBatchRepository extends JpaRepository<NotificationBatch, String> {
    Optional<NotificationBatch> findByTenantIdAndId(String tenantId, String id);
}
