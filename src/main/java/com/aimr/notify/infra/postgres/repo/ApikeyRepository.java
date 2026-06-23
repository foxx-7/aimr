package com.aimr.notify.infra.postgres.repo;

import com.aimr.notify.domain.entity.ApiKey;
import com.aimr.notify.domain.enums.ApiKeyStatus;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApikeyRepository extends JpaRepository<@NonNull ApiKey, @NonNull String> {
    List<ApiKey> findAllByTenantIdAndUserIdAndStatus(String tenantId, String userId, ApiKeyStatus Active);
    Optional<ApiKey> findByHashedKeyAndStatus(String hashedKey, ApiKeyStatus status);
}
