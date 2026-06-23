package com.aimr.notify.consumer.channel.email.function;

import com.aimr.notify.infra.postgres.dao.BindingDao;
import com.aimr.notify.infra.postgres.dao.SenderIdentityDao;
import com.aimr.notify.exception.DataTransportException;
import com.aimr.notify.exception.ResourceNotFoundException;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.consumer.dto.ChannelDispatchDTO;
import com.aimr.notify.consumer.dto.EmailDispatchDto;
import com.aimr.notify.domain.entity.Binding;
import com.aimr.notify.domain.entity.SenderIdentity;
import com.aimr.notify.domain.enums.NotificationStatus;
import com.aimr.notify.consumer.channel.email.provider.impl.GenericBackUpEmailProvider;
import com.aimr.notify.consumer.channel.email.provider.impl.GenericPrimaryEmailProvider;
import com.aimr.notify.service.NotificationService;
import com.aimr.notify.consumer.util.TemplateRenderer;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailDeliveryService {

    private final GenericPrimaryEmailProvider genericPrimaryEmailProvider;
    private final GenericBackUpEmailProvider genericBackUpEmailProvider;
    private final NotificationService notificationService;
    private final EmailRetryPublisher retryPublisher;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final BindingDao bindingDao;
    private final SenderIdentityDao senderIdentityDao;

    public void process(ChannelDispatchDTO dispatch, int retryCount, boolean isInitialAttempt) {

        //aware of unused isInitialAttempt variable;

        String requestId = dispatch.getRequestId();
        String tenantId = dispatch.getTenantId();


        String mailId = null;
        //idempotency check here
        try {
            //build message here
            Binding binding = bindingDao.fetchBindingByTenantIdAndName(tenantId, dispatch.getRecipientBinding())
                    .orElseThrow(()-> new ResourceNotFoundException(
                            "binding not found for recipient "+dispatch.getRecipientBinding()));

            SenderIdentity identity = senderIdentityDao.fetchIdentityByTenantIdAndId(tenantId,dispatch.getSenderIdentity())
                    .orElseThrow(()->new ResourceNotFoundException((
                            "sender identity not found for sender "+dispatch.getSenderIdentity())));
           //validate returns
            EmailDispatchDto emailDispatch = EmailDispatchDto.builder()
                    .tenantId(tenantId)
                    .requestId(requestId)
                    .recipientAddress(binding.getBindingAddress())
                    .senderName(identity.getSenderName())
                    .senderAddress(identity.getSenderAddress())
                    .subject(dispatch.getSubject())
                    .message(TemplateRenderer.render(dispatch.getMessage(), dispatch.getDynamicVariables()))
                    .build();

            try {
                // Executed strictly outside of open long-lived DB transaction contexts
                //owns mime message
                mailId = genericPrimaryEmailProvider.sendEmail(emailDispatch);
            } catch (DataTransportException primaryEx) {
                log.warn("[EmailDeliveryService] Primary channel failed for requestId={}. Activating backup line.....", requestId);

                // FIX: Isolate and protect the fallback pipeline from cascading outages
                String backupCbKey = "backup_".concat(tenantId);
                CircuitBreaker backupCb = circuitBreakerRegistry.circuitBreaker(backupCbKey);

                try {
                    mailId = backupCb.executeSupplier(() -> genericBackUpEmailProvider.sendEmail(emailDispatch));
                } catch (CallNotPermittedException cbEx) {
                    log.error("[Engine] Fallback pipeline circuit breaker open for key: {}. Routing to retry queue.", backupCbKey);
                    handleFailureRouting(dispatch, retryCount, "Backup circuit breaker active");
                    return;
                } catch (Exception backupEx) {
                    log.error("[Engine] Primary and backup pipelines completely exhausted for request: {}", requestId);
                    handleFailureRouting(dispatch, retryCount, backupEx.getMessage());
                    return;
                }
            }

            // ONE SINGLE SOURCE OF TRUTH: Single location updating success states out-of-line with network blocks
            notificationService.markNotificationStatus(tenantId, requestId, mailId, NotificationStatus.DELIVERED);

        } catch (ValidationException ve) {
            log.error("[EmailDeliveryService] Validation failed for request: {}. Halting.", requestId, ve);
            throw ve;
        } catch (ResourceNotFoundException re){
            log.error("[EmailDispatchService] Missing resources for processing request: {}.Halting...", requestId, re);
            throw re;
        } catch (Exception e) {
            log.error("[EmailDeliveryService] Unexpected error processing request: {}", requestId, e);
            handleFailureRouting(dispatch, retryCount, e.getMessage());
        }
    }

    private void handleFailureRouting(ChannelDispatchDTO dispatch, int retryCount, String internalReason) {
        notificationService.markNotificationStatus(
                dispatch.getTenantId(), 
                dispatch.getRequestId(), 
                null, 
                NotificationStatus.PENDING_RETRY
        );
        retryPublisher.publishToRetry(dispatch, retryCount + 1, internalReason);
    }
}