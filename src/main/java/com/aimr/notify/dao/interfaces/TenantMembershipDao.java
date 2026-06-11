package com.aimr.notify.dao.interfaces;

import com.aimr.notify.model.entity.TenantMembership;

import java.util.Optional;

public interface TenantMembershipDao {
    Optional<TenantMembership> findMembershipByUserIdAndTenantId(String userId, String tenantId);

    void saveMembership(TenantMembership membership);

    void deleteMembership(TenantMembership membership);
}
