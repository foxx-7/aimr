package com.aimr.notify.pubsub.channel.email.provider.interfaces;

import com.aimr.notify.model.dto.EmailDispatchDto;

public interface RetryEmailProvider {
    String sendEmail(EmailDispatchDto emailDispatchDto);
}
