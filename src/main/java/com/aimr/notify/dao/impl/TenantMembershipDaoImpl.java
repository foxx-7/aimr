package com.aimr.notify.dao.impl;

import com.aimr.notify.dao.interfaces.TenantMembershipDao;
import com.aimr.notify.dao.repostiories.jpa.TenantMembershipRepository;
import com.aimr.notify.model.entity.TenantMembership;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TenantMembershipDaoImpl implements TenantMembershipDao {

    private final TenantMembershipRepository tenantMembershipRepository;

    @Override
    public Optional<TenantMembership> findMembershipByUserIdAndTenantId(final String userId, final String tenantId){
        return tenantMembershipRepository.findByUserIdAndTenantId(userId, tenantId);
    }

    @Override
    public void saveMembership(final TenantMembership membership){
        tenantMembershipRepository.save(membership);
    }

    @Override
    public void deleteMembership(final TenantMembership membership){
        tenantMembershipRepository.delete(membership);
    }
}
