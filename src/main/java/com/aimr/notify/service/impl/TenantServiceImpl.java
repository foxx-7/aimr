package com.aimr.notify.service.impl;

import com.aimr.notify.aop.ValidateTenant;
import com.aimr.notify.dao.interfaces.CacheService;
import com.aimr.notify.dao.interfaces.TenantDao;
import com.aimr.notify.dao.interfaces.TenantMembershipDao;
import com.aimr.notify.dao.interfaces.ApiKeyDao;
import com.aimr.notify.exception.ResourceNotFoundException;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.model.dto.request.RegisterTenantRequest;
import com.aimr.notify.model.dto.response.TenantResponse;
import com.aimr.notify.model.entity.Tenant;
import com.aimr.notify.model.entity.TenantMembership;
import com.aimr.notify.model.enums.MembershipStatus;
import com.aimr.notify.model.enums.Role;
import com.aimr.notify.service.interfaces.TenantService;
import com.aimr.notify.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

import static com.aimr.notify.constant.ErrorConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantServiceImpl implements TenantService {

    private final TenantDao tenantDao;
    private final TenantMembershipDao membershipDao;
    private final ApiKeyDao apiKeyDao;
    private final CacheService cacheService;

    @Override
    @Transactional
    public TenantResponse registerTenant(String ownerId, RegisterTenantRequest request) {

        if(ownerId == null){
            throw new ValidationException(MISSING_TENANT_OWNER_ID_ERROR,
                    HttpStatus.BAD_REQUEST.value());
        }
        if(tenantDao.tenantExistsByName(request.getName().toLowerCase())){
            throw new ValidationException(TENANT_ALREADY_EXISTS_ERROR,
                    HttpStatus.BAD_REQUEST.value());
        }

        Tenant tenant = Tenant.builder()
                .id(CommonUtils.generateUUIDv7())
                .name(request.getName().toLowerCase())
                .ownerId(ownerId)
                .build();

        tenantDao.saveTenant(tenant);
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

        membershipDao.saveMembership(membership);
 log.info("[TenantService] Registering new tenant membership for userId: {}, tenantId: {}"
            ,tenant.getOwnerId(), tenant.getId());

        return new TenantResponse(tenant);
    }

    @Override
    public void deleteTenant(String id, String ownerId) {
        Tenant tenant = tenantDao.findTenantByIdAndOwnerId(id, ownerId).orElseThrow(() ->
                new ResourceNotFoundException(TENANT_NOT_FOUND_ERROR));
        tenantDao.deleteTenant(tenant);
    }

    @Override
    @ValidateTenant
    public TenantResponse updateTenant(String id, String ownerId, Map<String, Object> update) {
        Tenant tenant = tenantDao.findTenantByIdAndOwnerId(id, ownerId).orElseThrow(() ->
                new ResourceNotFoundException(TENANT_NOT_FOUND_ERROR, HttpStatus.NOT_FOUND.value()));

        update.forEach((key, value) -> {
            switch (key) {
                case "name" -> {
                    String name = (String) value;
                    if (name.isBlank())
                        throw new ValidationException(BLANK_NAME_FIELD_ERROR, HttpStatus.BAD_REQUEST.value());
                    tenant.setName(name.toLowerCase());
                }
                default -> throw new ValidationException(INVALID_UPDATE_FIELD_ERROR, HttpStatus.NOT_FOUND.value());
            }
        });

        tenantDao.saveTenant(tenant);
        return new TenantResponse(tenant);
    }

    @ValidateTenant
    @Override
    public String generateNewApiKey(final String name, final String userId) {
        String tenantId = CommonUtils.getCurrentTenantId();

        membershipDao.findMembershipByUserIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ValidationException(
                        "User: " + userId + " is not a member of tenant: " + tenantId,
                        HttpStatus.FORBIDDEN.value()));

        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);

        //key prefix for api keys will be reviewed to check necessity
        String rawKey = "ak_live_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String hashedKey = CommonUtils.generateSHA256Hash(rawKey);
        String keyPrefix = rawKey.substring(0, 12);

        apiKeyDao.saveNewApiKey(name, keyPrefix, tenantId, userId, hashedKey);
        return rawKey.substring(8);
    }
}
