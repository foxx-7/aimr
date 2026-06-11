package com.aimr.notify.dao.impl;

import com.aimr.notify.dao.interfaces.CacheService;
import com.aimr.notify.dao.interfaces.TenantDao;
import com.aimr.notify.dao.repostiories.jpa.TenantRepository;
import com.aimr.notify.model.entity.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TenantDaoImpl implements TenantDao {

    private final CacheService cacheService;
    private final TenantRepository tenantRepository;

    @Override
    public Tenant saveTenant(final Tenant tenant){
        return tenantRepository.save(tenant);
    }

    @Override
    public Optional<Tenant> findTenantById(final String id){
        return tenantRepository.findById(id);
    }

    @Override
    public Optional<Tenant> findTenantByNameAndOwnerId(final String name, final String ownerId){
        return tenantRepository.findByNameAndOwnerId(name, ownerId);
    }

    @Override
    public Optional<Tenant> findTenantByIdAndOwnerId(final String id, final String ownerId){
        return tenantRepository.findByIdAndOwnerId(id, ownerId);
    }

    @Override
    public void deleteTenant(final Tenant tenant){
        tenantRepository.delete(tenant);
    }

    @Override
    public boolean tenantExistsByName(final String name){
        return tenantRepository.existsByName(name);
    }
}
