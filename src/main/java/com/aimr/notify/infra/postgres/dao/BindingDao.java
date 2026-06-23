package com.aimr.notify.infra.postgres.dao;

import com.aimr.notify.domain.dao.UpdatableDao;
import com.aimr.notify.infra.postgres.repo.BindingRepository;
import com.aimr.notify.domain.entity.Binding;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BindingDao implements UpdatableDao<Binding> {
    private final BindingRepository bindingRepository;

    public Optional<Binding> fetchBindingByTenantIdAndName(final String tenantId, final String name){
        return bindingRepository.findByTenantIdAndName(tenantId, name);
    }

    public boolean bindingExistsByNameAndTenantId(final String name, final String tenantId){
        return fetchBindingByTenantIdAndName(tenantId, name).isPresent();
    }

    @Override
    public Optional<Binding> fetchEntity(final String tenantId, final String id){
        return bindingRepository.findByTenantIdAndId(tenantId, id);
    }

    @Override
    public Binding saveEntity(final Binding binding) {
        return save(binding);
    }
    private Binding save(final Binding binding){
        return bindingRepository.save(binding);
    }

    public void deleteBinding(final Binding binding){
        bindingRepository.delete(binding);
    }

    public List<Binding> fetchAllByTenantId(final String tenantId){
        return bindingRepository.findAllByTenantId(tenantId);
    }
}
