package com.aimr.notify.dao.repostiories.jpa;

import com.aimr.notify.models.entity.ApiKey;
import com.aimr.notify.models.enums.ApiKeyStatus;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApikeyRepository extends JpaRepository<@NonNull ApiKey, @NonNull String> {
    List<ApiKey> findAllByTenantIdAndUserIdAndStatus(String tenantId, String userId, ApiKeyStatus Active);

}
