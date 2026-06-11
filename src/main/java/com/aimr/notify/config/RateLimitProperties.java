package com.aimr.notify.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "aimr.notification.guard")
public record RateLimitProperties(

        /**
         * Per-channel rate limits (max requests per hour per tenant).
         * Key = channel name (lowercase), Value = max requests.
         * Example: { email: 50, sms: 30, push: 200 }
         */
        @NotNull
        Map<String, Integer> channelRateLimits,

        /**
         * Fallback limit used when a channel has no explicit entry in channelRateLimits.
         */
        @Positive
        int defaultRateLimit,

        /**
         * How long (in seconds) a dedup key lives in Redis.
         * After this window, the same notification can be sent again.
         * Default: 3600 (1 hour)
         */
        @Positive
        int dupWindowSeconds

) {
    /**
     * Returns the rate limit for the given channel,
     * falling back to defaultRateLimit if not configured.
     */
    public int limitFor(String channel) {
        if (channelRateLimits == null) {
            return defaultRateLimit;
        }
        return channelRateLimits.getOrDefault(channel.toLowerCase(), defaultRateLimit);
    }
}
