package com.aimr.notify.consumer.channel.email.provider.impl;

import com.aimr.notify.consumer.dto.EmailDispatchDto;
import com.aimr.notify.exception.DataTransportException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.UUID;

public abstract class AbstractEmailProvider {

    private final JavaMailSender mailSender;

    protected AbstractEmailProvider(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String deliver(EmailDispatchDto dto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(dto.getSenderAddress(), dto.getSenderName());
            helper.setTo(dto.getRecipientAddress());
            helper.setSubject(dto.getSubject());
            helper.setText(dto.getMessage(), true);

            mailSender.send(message);
            
            return UUID.randomUUID().toString();
        } catch (Exception e) {
            throw new DataTransportException("Failed to deliver email: " + e.getMessage());
        }
    }
}