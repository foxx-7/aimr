package com.aimr.notify.consumer.channel.email.provider.impl;

import com.aimr.notify.consumer.channel.email.provider.interfaces.BackUpEmailProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SendGridEmailProvider extends AbstractEmailProvider implements BackUpEmailProvider {

    public SendGridEmailProvider(@Qualifier("secondaryMailSender") JavaMailSender mailSender) {
        super(mailSender);
    }

    @Override
    public String getName() { return "SENDGRID"; }
}