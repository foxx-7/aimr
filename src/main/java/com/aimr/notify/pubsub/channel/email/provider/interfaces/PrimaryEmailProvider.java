package com.aimr.notify.pubsub.channel.email.provider.interfaces;

import com.aimr.notify.model.dto.EmailDispatchDto;

public interface PrimaryEmailProvider {
    String sendEmail(EmailDispatchDto emailDispatchDto);
}
