package com.aimr.notify.service;

import com.aimr.notify.domain.aop.annotation.ValidateTenant;
import com.aimr.notify.infra.mongo.dao.NotificationDao;
import com.aimr.notify.infra.mongo.dao.TemplateDao;
import com.aimr.notify.exception.ResourceNotFoundException;
import com.aimr.notify.consumer.dto.BroadcastTopicDTO;
import com.aimr.notify.api.dto.response.MongoSearchResult;
import com.aimr.notify.consumer.dto.IngestTopicDTO;
import com.aimr.notify.api.dto.response.ChannelStatusCount;
import com.aimr.notify.api.dto.response.ChannelSummary;
import com.aimr.notify.api.dto.response.NotificationSummary;
import com.aimr.notify.domain.entity.IdempotencyKey;
import com.aimr.notify.domain.entity.Notification;
import com.aimr.notify.api.dto.request.SendNotificationRequest;
import com.aimr.notify.api.dto.response.NotificationResponse;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.domain.entity.Template;
import com.aimr.notify.domain.enums.BatchStatus;
import com.aimr.notify.domain.enums.NotificationChannel;
import com.aimr.notify.domain.enums.NotificationStatus;
import com.aimr.notify.domain.enums.SummaryWindow;
import com.aimr.notify.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;
import com.aimr.notify.infra.postgres.dao.NotificationBatchDao;
import com.aimr.notify.domain.entity.NotificationBatch;

import java.time.Instant;
import java.util.*;

import static com.aimr.notify.constant.ErrorConstants.TEMPLATE_NOT_FOUND_ERROR;
import static com.aimr.notify.domain.enums.NotificationStatus.*;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final TemplateDao templateDao;
    private final NotificationPublisher notificationPublisher;
    private final NotificationDao notificationDao;
    private final NotificationBatchDao batchDao;
    private final RateLimiter rateLimiter;
    private final JsonMapper jsonMapper;

    @ValidateTenant
    public String sendNotification(final SendNotificationRequest request) {// <---- queue entry point here

        String tenantId = CommonUtils.getCurrentTenantId();

        //rate limit implementation per notification request
        //rateLimiter.checkAndGuard(
                //tenantId,
                //request.dispatchChannel().getValue(),
                //request.templateId(),
                //request.dynamicVariables());

        String templateId = request.templateId();

        // validate template exists
        Template template = templateDao.fetchTemplateByTenantIdAndId(tenantId, templateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        TEMPLATE_NOT_FOUND_ERROR, HttpStatus.BAD_REQUEST.value()));

        // validate request variables match template variables
        if (!request.dynamicVariables().keySet().containsAll(template.getTemplateVariables().keySet())) {
            throw new ValidationException("Invalid Dynamic Variables");
        }

        String requestId = CommonUtils.generateUUIDv4();
        String traceId = CommonUtils.getCurrentTraceId();

        Notification notification = Notification.builder()
                .id(CommonUtils.generateUUIDv7())
                .traceId(traceId)
                .requestId(requestId)
                .tenantId(tenantId)
                .recipientBinding(request.recipientBinding())
                .senderIdentity(request.senderIdentityId())
                .dispatchChannel(request.dispatchChannel())
                .templateId(request.templateId())
                .status(IN_FLIGHT)
                .build();
        notification.entityCreated();

        IngestTopicDTO ingestTopicDTO = IngestTopicDTO.builder()
                .requestId(requestId)
                .tenantId(tenantId)//tenant id for context isolation
                .channel(request.dispatchChannel())
                .dynamicVariables(request.dynamicVariables())//custom recipient details
                .templateId(request.templateId())//template to use
                .senderIdentity(request.senderIdentityId())
                .recipientBinding(request.recipientBinding())
                .subject(template.getName())
                .message(template.getMessageTemplate())
                .build();
                
                //may need to implement outbox pattern here and write one transaction writing to mongodb and ingest table

        try {
            notificationDao.saveNotification(notification);
            boolean published = notificationPublisher.sendDataToIngest(ingestTopicDTO);
            if (published) {
                notification.setStatus(QUEUED);
                notificationDao.saveNotification(notification);
            }else {
                log.error("[KafkaPublisher] failed to publish notification | requestId: {}", notification.getRequestId());
                notification.setStatus(AWAITING_QUEUE);
                notificationDao.saveNotification(notification);
            }
        } catch (Exception e) {
            notification.setStatus(AWAITING_QUEUE);
            notificationDao.saveNotification(notification);
        }
        return notification.getRequestId();
    }

    @ValidateTenant
    public String broadcastNotification(final SendNotificationRequest request) {

        String tenantId = CommonUtils.getCurrentTenantId();

        // Check how many batches are currently in-flight for this tenant
        // Reject if over limit (e.g. 2 concurrent broadcasts per tenant)
        rateLimiter.checkBroadcastInflight(tenantId);

        // Validate template exists
        String templateId = request.templateId();
        Template template = templateDao.fetchTemplateByTenantIdAndId(tenantId, templateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        TEMPLATE_NOT_FOUND_ERROR, HttpStatus.BAD_REQUEST.value()));

        // Validate dynamic variables match template variables
        if (!request.dynamicVariables().keySet().containsAll(template.getTemplateVariables().keySet())) {
            throw new ValidationException("Invalid Dynamic Variables");
        }

        // Build and persist the Batch document
        String batchId = CommonUtils.generateUUIDv7();
        String traceId = CommonUtils.getCurrentTraceId();

        NotificationBatch batch = NotificationBatch.builder()
                .id(batchId)
                .traceId(traceId)
                .tenantId(tenantId)
                .channel(request.dispatchChannel())
                .templateId(templateId)
                .senderIdentity(request.senderIdentityId())
                .dynamicVariables(request.dynamicVariables())
                .target(null)       // ALL or SEGMENT
                .segmentId(null)          // null if target = ALL
                .status(BatchStatus.PENDING)
                .processedCount(0L)
                .failedCount(0L)
                .build();
        batch.entityCreated();

        // Build the Kafka event for fan-out consumer
        BroadcastTopicDTO broadcastTopicDTO = BroadcastTopicDTO.builder()
                .batchId(batchId)
                .tenantId(tenantId)
                .channel(request.dispatchChannel())
                .templateId(templateId)
                .senderIdentity(request.senderIdentityId())
                .dynamicVariables(request.dynamicVariables())
                .subject(template.getName())
                .message(template.getMessageTemplate())
                .target(null) // FIXME: Add back target
                .segmentId(null) // FIXME: Add back segmentId
                .build();

        try {
            batchDao.saveBatch(batch);
            boolean published = notificationPublisher.sendDataToBroadcast(broadcastTopicDTO);
            if (published) {
                batch.setStatus(BatchStatus.QUEUED);
                batchDao.saveBatch(batch);
            } else {
                log.error("[BroadcastPublisher] failed to publish broadcast | batchId: {}", batchId);
                batch.setStatus(BatchStatus.PENDING);
                batchDao.saveBatch(batch);
            }
        } catch (Exception e) {
            log.error("[BroadcastPublisher] exception publishing broadcast | batchId: {}", batchId, e);
            batch.setStatus(BatchStatus.PENDING);
            batchDao.saveBatch(batch);
        }

        return batchId;
    }

    @ValidateTenant
    public MongoSearchResult<NotificationResponse> browseNotification(
            final Instant anchorTime,
            final NotificationChannel channel,
            final NotificationStatus status,
            final String cursor
    ) {
        String tenantId = CommonUtils.getCurrentTenantId();
        return notificationDao.filterNotification(tenantId, anchorTime,cursor,status,channel);
    }


    @ValidateTenant
    public NotificationSummary getNotificationSummary(SummaryWindow window) {

        //summary window boundary definition
        Instant from = window.toInstant();
        Instant to   = Instant.now();

        String tenantId = CommonUtils.getCurrentTenantId();

        List<ChannelStatusCount> counts = notificationDao
                .getStatusCountsByChannel(tenantId, from);

        // Group by channel
        Map<NotificationChannel, ChannelSummary> byChannel = Arrays
                .stream(NotificationChannel.values())
                .collect(toMap(
                        channel -> channel,
                        channel -> buildChannelSummary(channel, counts)
                ));

        long grandTotal = byChannel.values().stream()
                .mapToLong(ChannelSummary::totalSent).sum();

        long grandDelivered = byChannel.values().stream()
                .mapToLong(ChannelSummary::totalDelivered).sum();

        double overallRate = grandTotal > 0
                ? (double) grandDelivered / grandTotal * 100
                : 0.0;

        return new NotificationSummary(window, from, to, byChannel, grandTotal, overallRate);
    }

    //build channel summary functionaity
    private ChannelSummary buildChannelSummary(
            NotificationChannel channel,
            List<ChannelStatusCount> counts
    ) {
        Map<NotificationStatus, Long> statusMap = counts.stream()
                .filter(c -> c.channel() == channel)
                .collect(toMap(ChannelStatusCount::status, ChannelStatusCount::count));

        long delivered = statusMap.getOrDefault(DELIVERED, 0L);
        long failed    = statusMap.getOrDefault(FAILED, 0L);
        long inFlight   = statusMap.getOrDefault(IN_FLIGHT, 0L);
        long awaitingQueue = statusMap.getOrDefault(AWAITING_QUEUE, 0L);
        long enqueued = statusMap.getOrDefault(QUEUED, 0L);
        long pendingRetry = statusMap.getOrDefault(PENDING_RETRY, 0L);

        long pending = inFlight + awaitingQueue + enqueued + pendingRetry;
        long total     = delivered + failed + pending;
        double rate    = total > 0 ? (double) delivered / total * 100 : 0.0;

        return new ChannelSummary(channel, total, delivered, failed, pending, rate);
    }

    public String getNotificationMetrics(){

        return "hello";
    }

    @ValidateTenant
    public void markNotificationStatus(String tenantId, String requestId, String mailId, NotificationStatus status) {
        notificationDao.fetchNotificationByTenantIdAndRequestId(tenantId, requestId)
                .ifPresentOrElse(
                        notification -> {
                            notification.setStatus(status);
                            if (mailId != null) notification.setDispatchId(mailId);
                            notificationDao.saveNotification(notification);
                        },
                        () -> log.warn("[NotificationService] Notification not found, cannot mark {} for requestId={}", status, requestId)
                );
    }

}
