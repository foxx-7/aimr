package com.aimr.notify.pubsub.channel.email.service.interfaces;

import com.aimr.notify.models.dto.ChannelDispatchDTO;

public interface EmailDeliveryService {
    void process(ChannelDispatchDTO dispatch, int retryCount, boolean processWithPrimary);
}
