package com.aimr.notify.infra.mongo.repo;

import com.aimr.notify.domain.entity.Notification;
import com.aimr.notify.domain.enums.NotificationStatus;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NotificationRepository extends MongoRepository<@NonNull Notification, @NonNull String> {

    @Query(value = "{ 'tenantId': ?0, 'requestId': ?1 }", fields = "{'status': 1, '_id': 0}")
    Optional<NotificationStatus> findStatusByTenantIdAndRequestId(String tenantId, String requestId);
    Optional<Notification> findByTenantIdAndRequestId(String tenantId, String RequestId);
}
