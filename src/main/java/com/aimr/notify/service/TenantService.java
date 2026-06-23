package com.aimr.notify.service;

import com.aimr.notify.api.dto.request.UpdateTenantRequest;
import com.aimr.notify.api.util.EntityUpdater;
import com.aimr.notify.domain.aop.annotation.ValidateTenant;
import com.aimr.notify.infra.postgres.dao.ApiKeyDao;
import com.aimr.notify.infra.redis.cache.CacheService;
import com.aimr.notify.infra.postgres.dao.TenantDao;
import com.aimr.notify.infra.postgres.dao.TenantMembershipDao;
import com.aimr.notify.exception.ResourceNotFoundException;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.api.dto.request.RegisterTenantRequest;
import com.aimr.notify.api.dto.response.TenantResponse;
import com.aimr.notify.domain.entity.Tenant;
import com.aimr.notify.domain.entity.TenantMembership;
import com.aimr.notify.domain.enums.MembershipStatus;
import com.aimr.notify.domain.enums.Role;
import com.aimr.notify.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

import static com.aimr.notify.constant.ErrorConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantDao tenantDao;
    private final TenantMembershipDao membershipDao;
    private final ApiKeyDao apiKeyDao;
    private final CacheService cacheService;

    @Transactional
    public TenantResponse registerTenant(String ownerId, RegisterTenantRequest request) {

        if(ownerId == null){
            throw new ValidationException(MISSING_TENANT_OWNER_ID_ERROR,
                    HttpStatus.BAD_REQUEST.value());
        }
        if(tenantDao.tenantExistsByName(request.name().toLowerCase())){
            throw new ValidationException(TENANT_ALREADY_EXISTS_ERROR,
                    HttpStatus.BAD_REQUEST.value());
        }

        Tenant tenant = Tenant.builder()
                .id(CommonUtils.generateUUIDv7())
                .name(request.name().toLowerCase())
                .ownerId(ownerId)
                .build();

        tenantDao.saveEntity(tenant);
        log.info("[TenantService] Creating new tenant with tenant id: {}, ownerId: {}"
                , tenant.getId(), tenant.getOwnerId());

        TenantMembership membership = TenantMembership.builder()
                .id(CommonUtils.generateUUIDv7())
                .userId(ownerId)
                .tenantId(tenant.getId())
                .role(Role.OWNER)
                .invitedByUserId(ownerId)
                .status(MembershipStatus.ACTIVE)
                .build();

        membershipDao.saveEntity(membership);
 log.info("[TenantService] Registering new tenant membership for userId: {}, tenantId: {}"
            ,tenant.getOwnerId(), tenant.getId());

        return TenantResponse.from(tenant);
    }

    @Transactional
    public void deleteTenant(String id, String ownerId) {
        Tenant tenant = tenantDao.fetchTenantByIdAndOwnerId(id, ownerId).orElseThrow(() ->
                new ResourceNotFoundException(TENANT_NOT_FOUND_ERROR));

        //Todo:cascade delete across dbs
        tenantDao.deleteTenant(tenant);
    }

    public TenantResponse updateTenant(UpdateTenantRequest update) {
        Tenant tenant = EntityUpdater.updateForEntity(
                tenantDao,
                CommonUtils.getCurrentTenantId(),
                update
        );
        return TenantResponse.from(tenant);
    }

    @ValidateTenant
    public String generateNewApiKey(final String name, final String userId) {
        String tenantId = CommonUtils.getCurrentTenantId();

        membershipDao.fetchMembershipByUserIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ValidationException(
                        "User: " + userId + " is not a member of tenant: " + tenantId,
                        HttpStatus.FORBIDDEN.value()));

        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);

        //key prefix for api keys will be reviewed to check necessity
        String rawKey = "ak_live_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String hashedKey = CommonUtils.generateSHA256Hash(rawKey);
        String keyPrefix = rawKey.substring(0, 12);

        apiKeyDao.saveApiKey(name, keyPrefix, tenantId, userId, hashedKey);
        return rawKey.substring(8);
    }
}
