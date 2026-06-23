package com.aimr.notify.infra.postgres.dao;

import com.aimr.notify.infra.postgres.repo.ApikeyRepository;
import com.aimr.notify.domain.entity.ApiKey;
import com.aimr.notify.domain.enums.ApiKeyStatus;
import com.aimr.notify.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import com.aimr.notify.exception.ValidationException;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiKeyDao {

    private final ApikeyRepository apikeyRepository;

    public void saveApiKey(final String name, final String prefix, final String tenantId, final String userId, final String hashedKey){

        List<ApiKey> existingKeys = apikeyRepository.
                findAllByTenantIdAndUserIdAndStatus(tenantId,userId, ApiKeyStatus.ACTIVE);

        if(!existingKeys.isEmpty()){
           for(ApiKey key:existingKeys){
               key.setStatus(ApiKeyStatus.REVOKED);
           }
        }

        ApiKey apiKey = ApiKey.builder()
                .tenantId(CommonUtils.getCurrentTenantId())
                .userId(userId)
                .name(name)
                .keyPrefix(prefix)
                .hashedKey(hashedKey)
                .build();

        apikeyRepository.save(apiKey);
    }

    public ApiKey fetchApikeyByHashedKey(final String hashedKey) {
        ApiKey apiKey = apikeyRepository.findByHashedKeyAndStatus(hashedKey, ApiKeyStatus.ACTIVE)
            .orElseThrow(() -> new ValidationException("API key not found", HttpStatus.UNAUTHORIZED.value()));

        if (apiKey.getExpiresAt() != null && apiKey.getExpiresAt().isBefore(Instant.now())) {
            throw new ValidationException("API key has expired", HttpStatus.UNAUTHORIZED.value());
        }
        return apiKey;
    }
}
