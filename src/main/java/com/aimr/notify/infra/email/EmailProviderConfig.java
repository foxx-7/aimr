package com.aimr.notify.infra.email;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class EmailProviderConfig {

    private final MailProperties mailProperties;

    @Bean("primaryMailSender")
    public JavaMailSender primaryMailSender() {
        return buildSender(mailProperties.primary());
    }

    @Bean("secondaryMailSender")
    public JavaMailSender secondaryMailSender() {
        return buildSender(mailProperties.secondary());
    }

    private JavaMailSender buildSender(MailProperties.ProviderProperties props) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(props.host());
        sender.setPort(props.port());
        sender.setUsername(props.username());
        sender.setPassword(props.password());

        Properties javaMailProps = new Properties();
        javaMailProps.put("mail.smtp.auth", "true");
        javaMailProps.put("mail.smtp.starttls.enable", "true");
        sender.setJavaMailProperties(javaMailProps);

        return sender;
    }
}