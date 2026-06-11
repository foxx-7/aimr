package com.aimr.notify.dao.impl;

import com.aimr.notify.dao.interfaces.ApiKeyDao;
import com.aimr.notify.dao.repostiories.jpa.ApikeyRepository;
import com.aimr.notify.model.entity.ApiKey;
import com.aimr.notify.model.enums.ApiKeyStatus;
import com.aimr.notify.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiKeyDaoImpl implements ApiKeyDao {

    private final ApikeyRepository apikeyRepository;

    @Override
    public void saveNewApiKey(final String name, final String prefix, final String tenantId, final String userId, final String hashedKey){

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
}
