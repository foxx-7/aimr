package com.aimr.notify.pubsub.channel.email.provider.interfaces;

import com.aimr.notify.models.dto.EmailDispatchDto;

public interface EmailProvider {
    String send(EmailDispatchDto emailDispatchDto);
}
