package com.aimr.notify.pubsub.channel.email.service.impl;

import com.aimr.notify.exception.NotificationDispatchException;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.models.dto.EmailDispatchDto;
import com.aimr.notify.models.context.NotificationContext;
import com.aimr.notify.models.context.NotificationContextHolder;
import com.aimr.notify.models.dto.ChannelDispatchDTO;
import com.aimr.notify.models.enums.NotificationStatus;
import com.aimr.notify.models.entity.IdempotencyKey;
import com.aimr.notify.pubsub.channel.email.service.interfaces.EmailDeliveryService;
import com.aimr.notify.pubsub.channel.email.provider.interfaces.GenericBackUpEmailProvider;
import com.aimr.notify.pubsub.channel.email.provider.interfaces.GenericPrimaryEmailProvider;
import com.aimr.notify.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;


import static com.aimr.notify.constants.ApplicationConstants.MAX_EMAIL_RETRIES;
import static com.aimr.notify.constants.ApplicationConstants.X_REQUEST_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailDeliveryServiceImpl implements EmailDeliveryService {

    private final GenericBackUpEmailProvider genericBackUpEmailProvider;
    private final GenericPrimaryEmailProvider genericPrimaryEmailProvider;
    private final NotificationService notificationService;
    private final EmailRetryPublisher retryPublisher;

    @Override
    public void process(ChannelDispatchDTO dispatch, int retryCount, boolean processWithPrimary) {

        String requestId = dispatch.getRequestId();
        String tenantId = dispatch.getTenantId();
        String mailId;

        NotificationContextHolder.setContext(new NotificationContext(tenantId, false));
        MDC.put(X_REQUEST_ID, requestId);


        try {
            IdempotencyKey idempotencyKey = notificationService.getIdempotencyKey(tenantId, requestId);

            NotificationStatus status = idempotencyKey.getNotificationStatus();
            String key = idempotencyKey.getKey();

            if (status != NotificationStatus.DELIVERED) {
                if(key == null) {

                    notificationService.markNotificationStatus(tenantId, requestId, null, NotificationStatus.IN_FLIGHT);

                    EmailDispatchDto mail = new EmailDispatchDto(dispatch);

                    if (processWithPrimary) {
                        mailId = genericPrimaryEmailProvider.sendEmail(mail);
                    } else {
                        mailId = genericBackUpEmailProvider.sendEmail(mail);
                    }

                    //ToDo:ensure and track that primary and retry providers do not return an empty mailId;
                    if (mailId == null || mailId.isEmpty()) {
                        log.error("[EmailDeliveryService] Null mailId returned from provider for request: {}", requestId);
                        throw new NotificationDispatchException("mailId for request: " + requestId + " is null");
                    }
                    notificationService.markNotificationStatus(tenantId, requestId, mailId, NotificationStatus.DELIVERED);
                    log.info("[EmailDeliveryService] Delivered :requestId={}", requestId);

                }else {
                    log.warn("[EmailDeliveryService] Conflicting status report for request: {} with status: {} and mailId: {}"
                            ,requestId, status, key);
                }
            } else {
                String recipient = dispatch.getDynamicVariables().get("TO");
                log.warn("[Tenant: {}] Mail already sent to {} | notificationId: {} ,mailId: {} (SKIPPING......)",
                        tenantId, recipient, requestId, key);
            }

            //we need to determine the actual cause of the exception from the providers response code
        } catch (ValidationException e) {

            // Terminal — retry forbidden
            log.error("[EmailDeliveryService] Terminal failure | requestId: {}", requestId, e);
            notificationService.markNotificationStatus(tenantId, requestId, null, NotificationStatus.FAILED);
        } catch (NotificationDispatchException e) {

            // Retryable — hand off to retry publisher
            log.warn("[EmailDeliveryService] Retryable failure | requestId: {} ,attempt: {}/{}",
                    requestId, retryCount, MAX_EMAIL_RETRIES);
            notificationService.markNotificationStatus(tenantId, requestId, null, NotificationStatus.FAILED);
            retryPublisher.publishToRetry(dispatch, retryCount + 1, e.getMessage());
        } catch (Exception e) {
            log.error("[EmailDeliveryService] Unexpected error | requestId: {}", requestId, e);
            notificationService.markNotificationStatus(tenantId, requestId, null, NotificationStatus.FAILED);
            retryPublisher.publishToRetry(dispatch, retryCount + 1, e.getMessage());
        } finally {
            NotificationContextHolder.clear();
            MDC.clear();
        }
    }
}