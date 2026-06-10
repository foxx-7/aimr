package com.aimr.notify.pubsub.channel.email.provider.interfaces;

import com.aimr.notify.models.dto.EmailDispatchDto;

public interface GenericBackUpEmailProvider {
    String sendEmail(EmailDispatchDto emailDispatchDto);
}
