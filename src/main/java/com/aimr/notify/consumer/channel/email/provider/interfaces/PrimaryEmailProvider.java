package com.aimr.notify.consumer.channel.email.provider.interfaces;

import com.aimr.notify.consumer.dto.EmailDispatchDto;

public interface PrimaryEmailProvider {
    String getName();
    String deliver(EmailDispatchDto dto);
}