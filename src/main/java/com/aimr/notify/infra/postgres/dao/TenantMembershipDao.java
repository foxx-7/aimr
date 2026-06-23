package com.aimr.notify.infra.postgres.dao;

import com.aimr.notify.domain.dao.UpdatableDao;
import com.aimr.notify.infra.postgres.repo.TenantMembershipRepository;
import com.aimr.notify.domain.entity.TenantMembership;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TenantMembershipDao implements UpdatableDao<TenantMembership> {

    private final TenantMembershipRepository tenantMembershipRepository;

    public Optional<TenantMembership> fetchMembershipByUserIdAndTenantId(final String userId, final String tenantId){
        return tenantMembershipRepository.findByUserIdAndTenantId(userId, tenantId);
    }

    @Override
    public TenantMembership saveEntity(final TenantMembership membership){
        return save(membership);
    }

    private TenantMembership save(final TenantMembership membership){
        return tenantMembershipRepository.save(membership);
    }

    public void deleteMembership(final TenantMembership membership){
        tenantMembershipRepository.delete(membership);
    }

    @Override
    public Optional<TenantMembership> fetchEntity(String tenantId, String id) {
        return tenantMembershipRepository.findByUserIdAndTenantId(id, tenantId);
    }
}
