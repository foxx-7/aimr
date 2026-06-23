package com.aimr.notify.infra.postgres.dao;

import com.aimr.notify.domain.dao.SingleIdUpdatableDao;
import com.aimr.notify.domain.dao.UpdatableDao;
import com.aimr.notify.infra.redis.cache.CacheService;
import com.aimr.notify.infra.postgres.repo.TenantRepository;
import com.aimr.notify.domain.entity.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TenantDao implements SingleIdUpdatableDao<Tenant> {

    private final CacheService cacheService;
    private final TenantRepository tenantRepository;

    public Tenant save(final Tenant tenant){
        return tenantRepository.save(tenant);
    }

    public List<Tenant> fetchAllTenantsByOwnerId(final String id){
        return tenantRepository.findAllByOwnerId(id);
    }

    public Optional<Tenant> findTenantByNameAndOwnerId(final String name, final String ownerId){
        return tenantRepository.findByNameAndOwnerId(name, ownerId);
    }

    public Optional<Tenant> fetchTenantByIdAndOwnerId(final String id, final String ownerId){
        return tenantRepository.findByIdAndOwnerId(id, ownerId);
    }

    public void deleteTenant(final Tenant tenant){
        tenantRepository.delete(tenant);
    }

    public boolean tenantExistsByName(final String name){
        return tenantRepository.existsByName(name);
    }

    @Override
    public Optional<Tenant> fetchEntity(String tenantId) {
       return tenantRepository.findById(tenantId);
    }

    @Override
    public Tenant saveEntity(Tenant tenant) {
        return save(tenant);
    }
}
