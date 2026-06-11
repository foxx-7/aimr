package com.aimr.notify.pubsub.channel.email.provider.interfaces;

import com.aimr.notify.model.dto.EmailDispatchDto;

public interface EmailProvider {
    String send(EmailDispatchDto emailDispatchDto);
}
