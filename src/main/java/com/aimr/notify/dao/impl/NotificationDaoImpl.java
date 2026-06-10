package com.aimr.notify.dao.impl;

import com.aimr.notify.dao.interfaces.NotificationDao;
import com.aimr.notify.dao.repostiories.mongo.NotificationRepository;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.models.dto.ChannelStatusCount;
import com.aimr.notify.models.enums.NotificationChannel;
import com.aimr.notify.models.entity.Notification;
import com.aimr.notify.models.dto.NotificationCursor;
import com.aimr.notify.models.enums.NotificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static com.aimr.notify.constants.ApplicationConstants.NOTIFICATION_SEARCH_PAGE_SIZE;
import static com.aimr.notify.constants.ApplicationConstants.NOTIFICATION_SEARCH_WINDOW_HOURS;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
@RequiredArgsConstructor
@Slf4j
public class NotificationDaoImpl implements NotificationDao {

    private final MongoTemplate mongoTemplate;
    private final NotificationRepository notificationRepository;

    @Override
    public List<Notification> searchNotification(
            final String tenantId, final Instant anchorTime,
            final NotificationCursor notificationCursor,
            final NotificationStatus status,
            final NotificationChannel channel){

        if(status==null && channel==null){
            log.warn("[NotificationDao] Request rejected as both type and status cannot be null for search request");
            throw new ValidationException("status and type cannot both be null", HttpStatus.BAD_REQUEST.value());
        }
        /*
            if status is null we'll browse by type and vice versa..
            if both are null ,reject request
         */

        //calculate search window duration around the anchor
        Instant lowerBound=anchorTime.minus(NOTIFICATION_SEARCH_WINDOW_HOURS, ChronoUnit.HOURS);
        Instant upperBound=anchorTime.plus(NOTIFICATION_SEARCH_WINDOW_HOURS, ChronoUnit.HOURS);


        //base criteria -> tenant scoping + time window
        Criteria baseCriteria= where("tenantId").is(tenantId)
                .and("createdAt")
                .gte(lowerBound).lte(upperBound);



        if(status==null){
            baseCriteria = baseCriteria.and("type").is(channel);
        } else if (channel==null) {
            baseCriteria = baseCriteria.and("status").is(status);
        }else {
            baseCriteria = baseCriteria.and("status").is(status)
                    .and("type").is(channel);
        }

        if(notificationCursor != null) {
            Criteria cursorCriteria = new Criteria().orOperator(
                    where("createdAt").lt(notificationCursor.getCreatedAt()),
                    where("createdAt").is(notificationCursor.getCreatedAt())
                            .and("_id").lt(notificationCursor.getId()));

            baseCriteria = new Criteria().andOperator(baseCriteria, cursorCriteria);
        }

        //query build and execution
        Query query=new Query(baseCriteria)
                .with(Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "_id")))
                .limit(NOTIFICATION_SEARCH_PAGE_SIZE+1);//fetch 51 items and determine if next page exists

        return mongoTemplate.find(query,Notification.class);
    }

    @Override
    public List<ChannelStatusCount> getStatusCountsByChannel(String tenantId, Instant from) {
        MatchOperation match = match(
                where("tenantId").is(tenantId)
                        .and("createdAt").gte(from)
        );

        GroupOperation group = group("channel", "status")
                .count().as("count");

        return mongoTemplate.aggregate(
                newAggregation(match, group),
                "notifications",
                ChannelStatusCount.class
        ).getMappedResults();
    } 

    @Override
    public void saveNotification(Notification notification){
        notificationRepository.save(notification);
    }

    @Override
    public Optional<Notification> fetchNotificationByTenantIdAndRequestId(
            final String tenantId, final String requestId){
        return notificationRepository.findByTenantIdAndRequestId(tenantId, requestId);
    }

    @Override
    public List<Notification> fetchForExport(
            final String tenantId, final NotificationChannel channel,
            final NotificationStatus status, final Instant from, final Instant to) {

        // base criteria — tenant + channel + date range
        Criteria criteria = where("tenantId").is(tenantId)
                .and("channel").is(channel)
                .and("createdAt").gte(from).lte(to);

        // append status only if provided
        if (status != null) {
            criteria = criteria.and("status").is(status);
        }

        Query query = new Query(criteria)
                .with(Sort.by(Sort.Direction.DESC, "createdAt"));

        return mongoTemplate.find(query, Notification.class);
    }
}
