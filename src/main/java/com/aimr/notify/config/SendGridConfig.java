package com.aimr.notify.config;

import com.sendgrid.SendGrid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SendGridConfig {

    private final ApplicationProperties properties;

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(properties.getSendgridApikey());
    }
}