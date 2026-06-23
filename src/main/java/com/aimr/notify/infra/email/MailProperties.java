package com.aimr.notify.infra.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(
    ProviderProperties primary,
    ProviderProperties secondary
) {
    public record ProviderProperties(
        String host,
        int port,
        String username,
        String password
    ) {}
}