package com.aimr.notify.pubsub.queue.publisher.interfaces;

public interface GenericFallbackPublisher {

    boolean sendNotification(String topic, String message);
}
