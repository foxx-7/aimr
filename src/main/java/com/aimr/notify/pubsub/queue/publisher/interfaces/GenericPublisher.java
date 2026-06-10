package com.aimr.notify.pubsub.queue.publisher.interfaces;

public interface GenericPublisher {

    boolean sendDataToIngest(Object input);

    boolean sendDataToAudit(Object input);

    boolean sendNotification(String topic, String message);
}
