package com.aimr.notify.pubsub.channel.email.provider.impl;

import com.aimr.notify.model.dto.EmailDispatchDto;
import com.aimr.notify.pubsub.channel.email.provider.interfaces.GenericPrimaryEmailProvider;
import com.aimr.notify.pubsub.channel.email.provider.interfaces.PrimaryEmailProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenericPrimaryEmailProviderImpl implements GenericPrimaryEmailProvider {

    private final List<PrimaryEmailProvider> emailProviders;

    @Override
    public String sendEmail(EmailDispatchDto emailDispatchDto){

        AtomicReference<String> mailId= new AtomicReference<>();
        AtomicBoolean success = new AtomicBoolean(false);

        emailProviders.forEach(primaryEmailProvider -> {
            mailId.set(primaryEmailProvider.sendEmail(emailDispatchDto));
            boolean isSent= mailId.get().isEmpty();
            if(!success.get()){
                success.set(isSent);
            }
            if(isSent){
                log.info("[PrimaryEmailProvider] mail sent using mail service provider {}",
                        primaryEmailProvider.getClass().getSimpleName());
            }else {
                log.error("[PrimaryEmailProvider] error sending mail using mail provider {}",
                        primaryEmailProvider.getClass().getSimpleName());
            }
        });
        return mailId.get();
    }
}
