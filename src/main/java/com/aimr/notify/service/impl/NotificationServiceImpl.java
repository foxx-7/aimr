package com.aimr.notify.service.impl;

import com.aimr.notify.aop.ValidateTenant;
import com.aimr.notify.dao.interfaces.NotificationDao;
import com.aimr.notify.exception.DataConversionException;
import com.aimr.notify.model.dto.*;
import com.aimr.notify.model.enums.*;
import com.aimr.notify.model.entity.IdempotencyKey;
import com.aimr.notify.model.entity.Notification;
import com.aimr.notify.model.dto.request.SendNotificationRequest;
import com.aimr.notify.model.dto.response.NotificationResponse;
import com.aimr.notify.model.dto.response.NotificationSearchResponse;
import com.aimr.notify.dao.interfaces.TemplateDao;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.exception.DataTransportException;
import com.aimr.notify.model.entity.Template;
import com.aimr.notify.pubsub.queue.publisher.interfaces.GenericPublisher;
import com.aimr.notify.service.interfaces.NotificationService;
import com.aimr.notify.service.interfaces.RateLimitService;
import com.aimr.notify.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

import static com.aimr.notify.constant.ApplicationConstants.*;
import static com.aimr.notify.constant.ErrorConstants.*;
import static com.aimr.notify.model.enums.NotificationStatus.*;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final TemplateDao templateDao;
    private final GenericPublisher genericPublisher;
    private final NotificationDao notificationDao;
    private final RateLimitService rateLimitService;
    private final JsonMapper jsonMapper;

    @Override
    @ValidateTenant
    public String sendNotification(final SendNotificationRequest request) {// <---- queue entry point here

        String tenantId = CommonUtils.getCurrentTenantId();

        //rate limit implementation per notification request
        rateLimitService.checkAndGuard(
                tenantId,
                request.getDispatchChannel().getValue(),
                request.getTemplateId(),
                request.getDynamicVariables());

        String templateId = request.getTemplateId();

        Optional<Template> byTenantIdAndId = templateDao.findTemplateByTenantIdAndId(tenantId, templateId);

        //validate for empty optional state
        if (byTenantIdAndId.isEmpty()) {
            throw new ValidationException(TEMPLATE_NOT_FOUND_ERROR, HttpStatus.BAD_REQUEST.value());
        }

        Template template = byTenantIdAndId.get();
        Map<String, String> requestDynamicVariables = request.getDynamicVariables();

            /*
                ensure the size of the size of the provided variables is not
                 more than that of the size of the registered template
                check if the provided dynamic variables match the variables in the registered template
             */
        if (!requestDynamicVariables.keySet().containsAll(template.getTemplateVariables().keySet())) {
            throw new ValidationException("Invalid Dynamic Variables");
        }

        String requestId = CommonUtils.generateUUIDv4();
        String traceId = CommonUtils.getCurrentTraceId();

        Notification notification = Notification.builder()
                .id(CommonUtils.generateUUIDv7())
                .traceId(traceId)
                .requestId(requestId)
                .tenantId(tenantId)
                .dispatchChannel(request.getDispatchChannel())
                .templateId(request.getTemplateId())
                .status(PENDING)
                .submittedAt(CommonUtils.getCurrentTimeStamp())
                .build();
        notification.entityCreated();
        notificationDao.saveNotification(notification);

        IngestTopicDTO ingestTopicDTO = IngestTopicDTO.builder()
                .requestId(requestId)
                .tenantId(tenantId)//tenant id for context isolation
                .channel(request.getDispatchChannel())
                .dynamicVariables(requestDynamicVariables)//custom recipient details
                .templateId(request.getTemplateId())//template to use
                .subject(template.getName())
                .message(template.getMessageTemplate())
                .build();

        try {
            boolean published = genericPublisher.sendDataToIngest(ingestTopicDTO);
            if (!published) {
                throw new DataTransportException(KAFKA_PUBLISH_FAILURE_ERROR);
            }
        } catch (Exception e) {
            notification.setStatus(FAILED);
            notificationDao.saveNotification(notification);
            if (e instanceof DataTransportException) {
                throw (DataTransportException) e;
            }
            throw new DataTransportException(KAFKA_PUBLISH_FAILURE_ERROR.concat(e.getMessage()));
        }
        return notification.getRequestId();
    }



    @ValidateTenant
    @Override
    public NotificationSearchResponse browseNotification(final Instant anchorTime, final NotificationChannel
            channel, final NotificationStatus status, final String cursor){

        //will be null initially
        NotificationCursor decodedCursor=decodeCursor(cursor);

        //fetch page size + 1 results from mongo db
        List<Notification> results=notificationDao.searchNotification(
                CommonUtils.getCurrentTenantId(), anchorTime, decodedCursor, status, channel);

        //determine if more pages exist
        int pageSize=NOTIFICATION_SEARCH_PAGE_SIZE;
        boolean hasMore=results.size()>pageSize;


        List<Notification> page=hasMore ? results.subList(0, pageSize)
                : results;

        String nextCursor=hasMore ? encodeCursor(page.getLast())
                :null;

        return NotificationSearchResponse.builder()
                .notifications(page.stream().map(NotificationResponse::new).toList())
                .hasMore(hasMore)
                .nextCursor(nextCursor)
                .build();

    }

    //cursor encoding
    private String encodeCursor(Notification last){
        try{
            NotificationCursor cursor=new NotificationCursor(last.getCreatedAt(),last.getId());
            String json=jsonMapper.writeValueAsString(cursor);
            return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        }catch (Exception e){
            throw new DataConversionException(CURSOR_ENCODING_FAILURE_ERROR);
        }
    }

    //cursor decoding
    private NotificationCursor decodeCursor(String encoding){
        if(encoding==null) return null;

        try{
           String json = new String(Base64.getDecoder().decode(encoding), StandardCharsets.UTF_8);
           return jsonMapper.readValue(json, NotificationCursor.class);
        }catch(Exception e){
            throw new DataConversionException(CURSOR_DECODING_FAILURE_ERROR);
        }
    }


    @ValidateTenant
    @Override
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
        long pending   = statusMap.getOrDefault(PENDING, 0L);
        long total     = delivered + failed + pending;
        double rate    = total > 0 ? (double) delivered / total * 100 : 0.0;

        return new ChannelSummary(channel, total, delivered, failed, pending, rate);
    }

    public String getNotificationMetrics(){

        return "hello";
    }

    @Override
    @ValidateTenant
    public void markNotificationStatus(String tenantId, String requestId, String mailId, NotificationStatus status) {
        notificationDao.fetchNotificationByTenantIdAndRequestId(tenantId, requestId)
                .ifPresentOrElse(
                        notification -> {
                            notification.setStatus(status);
                            if (mailId != null) notification.setMailId(mailId);
                            notificationDao.saveNotification(notification);
                        },
                        () -> log.warn("[NotificationServiceImpl] Notification not found, cannot mark {} for requestId={}", status, requestId)
                );
    }

    @Override
    @ValidateTenant
    public IdempotencyKey getIdempotencyKey(final String tenantId, final String requestId){
        Notification notification = notificationDao.fetchNotificationByTenantIdAndRequestId(tenantId, requestId)
                .orElseThrow(() -> new ValidationException(NOTIFICATION_NOT_FOUND_ERROR));
        return new IdempotencyKey(notification.getMailId(), notification.getStatus());
    }
}
