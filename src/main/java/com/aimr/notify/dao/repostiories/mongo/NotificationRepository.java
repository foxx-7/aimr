package com.aimr.notify.dao.repostiories.mongo;

import com.aimr.notify.model.entity.Notification;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends MongoRepository<@NonNull Notification, @NonNull String> {

    @Query("{'tenantId': ?0}")
    List<Notification> findAllByTenantId (String tenantId);
    Optional<Notification> findByTenantIdAndRequestId(String tenantId, String RequestId);
}
