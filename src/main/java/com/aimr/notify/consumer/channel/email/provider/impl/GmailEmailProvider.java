package com.aimr.notify.consumer.channel.email.provider.impl;

import com.aimr.notify.consumer.channel.email.provider.interfaces.PrimaryEmailProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class GmailEmailProvider extends AbstractEmailProvider implements PrimaryEmailProvider {

    // Manual constructor — @Qualifier doesn't work with Lombok
    public GmailEmailProvider(@Qualifier("primaryMailSender") JavaMailSender mailSender) {
        super(mailSender);
    }

    @Override
    public String getName() { return "GMAIL"; }
}