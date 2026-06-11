package com.aimr.notify.pubsub.channel.email.provider.impl;

import com.aimr.notify.model.dto.EmailDispatchDto;
import com.aimr.notify.pubsub.channel.email.provider.interfaces.GenericBackUpEmailProvider;
import com.aimr.notify.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenericBackUpEmailProviderImpl implements GenericBackUpEmailProvider {
    @Override
    public String sendEmail(EmailDispatchDto emailDispatchDto) {
        return CommonUtils.generateUUIDv4();
    }
}
