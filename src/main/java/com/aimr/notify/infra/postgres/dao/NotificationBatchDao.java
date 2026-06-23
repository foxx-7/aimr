package com.aimr.notify.infra.postgres.dao;

import com.aimr.notify.infra.postgres.repo.NotificationBatchRepository;
import com.aimr.notify.domain.entity.NotificationBatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class NotificationBatchDao {

    private final NotificationBatchRepository repository;

    public NotificationBatch saveBatch(NotificationBatch batch) {
        return repository.save(batch);
    }

    public Optional<NotificationBatch> fetchBatchByTenantIdAndId(final String tenantId, final String id) {
        return repository.findByTenantIdAndId(tenantId, id);
    }
}
