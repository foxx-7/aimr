package com.aimr.notify.pubsub.queue.provider.interfaces;

public interface GenericProvider {
    boolean sendNotification(String topic, String message);
}
