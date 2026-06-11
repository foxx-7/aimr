package com.aimr.notify.dao.repostiories.jpa;

import com.aimr.notify.model.entity.ApiKey;
import com.aimr.notify.model.enums.ApiKeyStatus;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApikeyRepository extends JpaRepository<@NonNull ApiKey, @NonNull String> {
    List<ApiKey> findAllByTenantIdAndUserIdAndStatus(String tenantId, String userId, ApiKeyStatus Active);

}
