package com.aimr.notify.service;

import com.aimr.notify.infra.postgres.dao.SenderIdentityDao;
import com.aimr.notify.exception.ResourceNotFoundException;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.api.dto.request.CreateSenderIdentityRequest;
import com.aimr.notify.api.dto.request.UpdateSenderIdentityRequest;
import com.aimr.notify.api.dto.response.SenderIdentityResponse;
import com.aimr.notify.domain.entity.SenderIdentity;
import com.aimr.notify.util.CommonUtils;
import com.aimr.notify.api.util.EntityUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.aimr.notify.constant.ErrorConstants.NOT_FOUND_ERROR_SUFFIX;

@Service
@RequiredArgsConstructor
public class SenderIdentityService {
    private final SenderIdentityDao senderIdentityDao;

    public SenderIdentityResponse createSenderIdentity(CreateSenderIdentityRequest request){
        String tenantId = CommonUtils.getCurrentTenantId();

        if(senderIdentityDao.senderExistsByTenantIdAndName(tenantId, request.name())){
            throw new ValidationException("sender identity already exists");
        }

        SenderIdentity identity = SenderIdentity.builder()
                .id(CommonUtils.generateUUIDv7())
                .tenantId(tenantId)
                .senderName(request.name())
                .senderAddress(request.address())
                .channel(request.channel())
                .build();

        senderIdentityDao.saveEntity(identity);

        return SenderIdentityResponse.from(identity);
    }

    public List<SenderIdentityResponse> getAllSenderIdentities(){
        return senderIdentityDao.fetchAllByTenantId(CommonUtils.getCurrentTenantId()).stream()
                .map(SenderIdentityResponse::from)
                .toList();
    }

    public SenderIdentityResponse updateSenderIdentity(
            final String identityId,
            final UpdateSenderIdentityRequest request
    ){
        SenderIdentity identity = EntityUpdater.updateForTenant(
                senderIdentityDao,
                CommonUtils.getCurrentTenantId(),
                identityId,
                request
        );
        return SenderIdentityResponse.from(identity);
    }

    public void deleteIdentityById(final String id){
        SenderIdentity identity = senderIdentityDao.fetchEntity(
                CommonUtils.getCurrentTenantId(), id).orElseThrow(
                ()->new ResourceNotFoundException(
                        SenderIdentity.class.getSimpleName()+NOT_FOUND_ERROR_SUFFIX));
        senderIdentityDao.deleteSenderIdentity(identity);
    }
}
