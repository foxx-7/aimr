package com.aimr.notify.pubsub.channel.email.provider.interfaces;

import com.aimr.notify.models.dto.EmailDispatchDto;

public interface GenericPrimaryEmailProvider {
    String sendEmail(EmailDispatchDto emailDispatchDto);
}
