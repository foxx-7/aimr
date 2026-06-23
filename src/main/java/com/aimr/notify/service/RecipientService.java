package com.aimr.notify.service;

import com.aimr.notify.infra.postgres.dao.BindingDao;
import com.aimr.notify.exception.ResourceNotFoundException;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.api.dto.request.CreateBindingRequest;
import com.aimr.notify.api.dto.request.UpdateBindingRequest;
import com.aimr.notify.api.dto.response.BindingResponse;
import com.aimr.notify.domain.entity.Binding;
import com.aimr.notify.util.CommonUtils;
import com.aimr.notify.api.util.EntityUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.aimr.notify.constant.ErrorConstants.NOT_FOUND_ERROR_SUFFIX;

@Service
@RequiredArgsConstructor
public class RecipientService {
    private final BindingDao bindingDao;

    public BindingResponse createNewRecipientBinding(final CreateBindingRequest request){
        String tenantId = CommonUtils.getCurrentTenantId();

        if(bindingDao.bindingExistsByNameAndTenantId(request.name(), tenantId)){
            throw new ValidationException("Binding with provided details already exists");
        }
        Binding binding = Binding.builder()
                .id(CommonUtils.generateUUIDv7())
                .tenantId(tenantId)
                .name(request.name())
                .bindingAddress(request.bindingAddress())
                .build();

        binding = bindingDao.saveEntity(binding);

        return BindingResponse.from(binding);
    }

    public List<BindingResponse> getAllBindings(){
        return bindingDao.fetchAllByTenantId(CommonUtils.getCurrentTenantId()).stream()
                .map(BindingResponse::from)
                .toList();
    }

    public BindingResponse updateBinding(
            final String bindingId,
            final UpdateBindingRequest request
    ){
        Binding binding = EntityUpdater.updateForTenant(
                bindingDao,
                CommonUtils.getCurrentTenantId(),
                bindingId,
                request
        );
        return BindingResponse.from(binding);
    }

    public void deleteBindingById(final String id){
        Binding binding = bindingDao.fetchEntity(
                CommonUtils.getCurrentTenantId(), id).orElseThrow(
                ()->new ResourceNotFoundException(
                        Binding.class.getSimpleName()+NOT_FOUND_ERROR_SUFFIX));
        bindingDao.deleteBinding(binding);
    }
}
