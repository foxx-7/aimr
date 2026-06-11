package com.aimr.notify.pubsub.channel.email.provider.interfaces;

import com.aimr.notify.model.dto.EmailDispatchDto;

public interface GenericPrimaryEmailProvider {
    String sendEmail(EmailDispatchDto emailDispatchDto);
}
