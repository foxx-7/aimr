package com.aimr.notify.infra.postgres.dao;

import com.aimr.notify.domain.dao.UpdatableDao;
import com.aimr.notify.infra.postgres.repo.SenderIdentityRepository;
import com.aimr.notify.domain.entity.SenderIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SenderIdentityDao implements UpdatableDao<SenderIdentity> {
    private final SenderIdentityRepository senderIdentityRepository;

    @Override
    public Optional<SenderIdentity> fetchEntity(final String tenantId, final String id){
        return fetchIdentityByTenantIdAndId(tenantId, id);
    }
    public Optional<SenderIdentity> fetchIdentityByTenantIdAndId(final String tenantId, final String id){
        return senderIdentityRepository.findByTenantIdAndId(tenantId, id);
    }
    public boolean senderExistsByTenantIdAndName(final String tenantId, final String name){
        return senderIdentityRepository.existsByTenantIdAndSenderName(tenantId, name);
    }

    @Override
   public SenderIdentity saveEntity(final SenderIdentity senderIdentity){
        return save(senderIdentity);
   }

   private SenderIdentity save(final SenderIdentity senderIdentity){
        return senderIdentityRepository.save(senderIdentity);
   }

   public void deleteSenderIdentity(final SenderIdentity identity){
        senderIdentityRepository.delete(identity);
   }

   public List<SenderIdentity> fetchAllByTenantId(final String tenantId){
        return senderIdentityRepository.findAllByTenantId(tenantId);
   }
}
