package com.aimr.notify.infra.mongo.dao;

import com.aimr.notify.infra.mongo.repo.NotificationRepository;
import com.aimr.notify.api.dto.response.ChannelStatusCount;
import com.aimr.notify.api.dto.response.MongoSearchResult;
import com.aimr.notify.api.dto.request.NotificationSearchDTO;
import com.aimr.notify.api.dto.response.NotificationResponse;
import com.aimr.notify.domain.enums.NotificationChannel;
import com.aimr.notify.domain.entity.Notification;
import com.aimr.notify.domain.enums.NotificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.aimr.notify.constant.ApplicationConstants.NOTIFICATION_SEARCH_WINDOW_HOURS;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
@RequiredArgsConstructor
@Slf4j
public class NotificationDao {

    private final MongoTemplate mongoTemplate;
    private final NotificationRepository notificationRepository;
    private final JsonMapper jsonMapper;

    public MongoSearchResult<NotificationResponse> filterNotification(
            final String tenantId, final Instant anchorTime,
            final String encodedCursor,
            final NotificationStatus status,
            final NotificationChannel channel){

       // Build DTO — decodes cursor + sets window guard in one call
        NotificationSearchDTO dto = NotificationSearchDTO.from(
                encodedCursor, anchorTime, NOTIFICATION_SEARCH_WINDOW_HOURS, jsonMapper);

        dto.setStatus(status);
        dto.setChannel(channel);

        // Build and execute query — already has limit(pageSize + 1) baked in
        Query query = dto.toQuery(tenantId);
        List<Notification> results = mongoTemplate.find(query, Notification.class);

        // Detect hasMore using the +1 result, then trim
        int pageSize = dto.resolvedPageSize();
        boolean hasMore = results.size() > pageSize;
        List<Notification> page = hasMore ? results.subList(0, pageSize) : results;

        // Encode cursor from the last document on this page
        String nextCursor = hasMore ? dto.encodeNextCursor(page.getLast(), jsonMapper) : null;

        // Wrap and return
        return MongoSearchResult.of(
                page.stream().map(NotificationResponse::from).toList(),
                page.size(), 0, pageSize, nextCursor
        );
    }

    public List<ChannelStatusCount> getStatusCountsByChannel(String tenantId, Instant from) {
        MatchOperation match = match(
                where("tenantId").is(tenantId)
                        .and("createdAt").gte(from)
        );

        GroupOperation group = group("dispatchChannel", "status")
                .count().as("count");

        ProjectionOperation project = project("count")
                .and("_id.dispatchChannel").as("channel")
                .and("_id.status").as("status");

        return mongoTemplate.aggregate(
                newAggregation(match, group, project),
                "notifications",
                ChannelStatusCount.class
        ).getMappedResults();
    }

    public void saveNotification(Notification notification){
         notificationRepository.save(notification);
    }

    public Optional<Notification> fetchEntityByTenantIdAndRequestId(
            final String tenantId, final String requestId){
        return notificationRepository.findByTenantIdAndRequestId(tenantId, requestId);
    }

    public List<Notification> fetchForExport(
            final String tenantId, final NotificationChannel channel,
            final NotificationStatus status, final Instant from, final Instant to) {

        // base criteria — tenant + channel + date range
        Criteria criteria = where("tenantId").is(tenantId)
                .and("dispatchChannel").is(channel)
                .and("createdAt").gte(from).lte(to);

        // append status only if provided
        if (status != null) {
            criteria = criteria.and("status").is(status);
        }

        Query query = new Query(criteria)
                .with(Sort.by(Sort.Direction.DESC, "createdAt"));

        return mongoTemplate.find(query, Notification.class);
    }

    public Optional<Notification> fetchNotificationByTenantIdAndRequestId(
            final String tenantId, final String requestId){
        return notificationRepository.findByTenantIdAndRequestId(tenantId, requestId);
    }

    public Optional<NotificationStatus> fetchNotificationStatus(final String tenantId, final String requestId){
        return notificationRepository.findStatusByTenantIdAndRequestId(tenantId,requestId);
    }
}
