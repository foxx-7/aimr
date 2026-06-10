package com.aimr.notify.pubsub.queue.provider.impl;

import com.aimr.notify.pubsub.queue.provider.interfaces.MqttProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(value = "messaging.fallback.mqtt.enabled", havingValue = "true")
class MqttProviderImpl implements MqttProvider {
    @Override
    public boolean sendNotification(String topic, String message) {
        log.info("Sending notification using mqtt for topic : {}", topic);
        return false;
    }
}
