package com.aimr.notify.dao.repostiories.jpa;

import com.aimr.notify.models.entity.TenantMembership;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantMembershipRepository extends
        JpaRepository<@NonNull TenantMembership, @NonNull String> {
    Optional<TenantMembership> findByUserIdAndTenantId(String userId, String tenantId);
}
