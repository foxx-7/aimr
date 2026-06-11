package com.aimr.notify.service.impl;

import com.aimr.notify.config.RateLimitProperties;
import com.aimr.notify.exception.RateLimitException;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.service.interfaces.RateLimitService;
import com.aimr.notify.util.CommonUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.aimr.notify.constant.ApplicationConstants.*;
import static com.aimr.notify.constant.ErrorConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<@NonNull List<Long>> notificationGuardScript;
    private final RateLimitProperties properties;

    /**
     * Performs a single atomic Redis check covering:
     *   1. Duplicate detection   — same tenantId + channel + templateId + variables within dupWindowSeconds
     *   2. Rate limit enforcement — per tenant per channel, max requests per hour
     * <p>
     * Must be called at the very start of sendNotification(), before any DB or Kafka work.
     *
     * @param tenantId   tenant making the request (from TenantContext)
     * @param channel    notification channel (e.g. "email", "sms", "push")
     * @param templateId template identifier used for this notification
     * @param variables  dynamic template variables map (order-insensitive)
     * @throws ValidationException   if an identical request was made within the window
     * @throws RateLimitException   if the tenant has exceeded the channel rate limit
     * @throws ValidationException      if the Redis script returns an unexpected result
     */
    @Override
    public void checkAndGuard(String tenantId,
                              String channel,
                              String templateId,
                              Map<String, String> variables) {

        String dedupKey = buildDedupKey(tenantId, channel, templateId, variables);
        String rlKey    = buildRateLimitKey(tenantId, channel);
        int    rlMax    = properties.limitFor(channel);
        int    dupTtl   = properties.dupWindowSeconds();

        log.debug("[Guard] Checking — tenantId={} channel={} templateId={} rlMax={} dupTtl={}s",
                tenantId, channel, templateId, rlMax, dupTtl);

        List<Long> result = redisTemplate.execute(
                notificationGuardScript,
                List.of(dedupKey, rlKey),
                String.valueOf(rlMax),
                String.valueOf(dupTtl),
                String.valueOf(RATE_LIMIT_WINDOW_SECONDS)
        );

        if (result.isEmpty()) {
            log.error("[NotificationGuardService] Redis Lua script returned null — tenantId={} channel={}", tenantId, channel);
            throw new ValidationException(NOTIFICATION_GUARD_CHECK_ERROR);
        }

        long status = result.get(0);
        long count  = result.get(1);

        switch ((int) status) {
            case 0 -> {
                int windowMinutes = properties.dupWindowSeconds() / 60;
                log.warn("[NotificationGuardService] Duplicate blocked — tenantId={} channel={} templateId={}",
                        tenantId, channel, templateId);
                throw new ValidationException(DUPLICATE_NOTIFICATION_REQUEST_ERROR.formatted(windowMinutes));
            }
            case -1 -> {
                log.warn("[NotificationGuardService] Rate limit breached — tenantId: {} ,channel: {} ,count: {} ,max: {}",
                        tenantId, channel, count, rlMax);
                throw new RateLimitException(CHANNEL_RATE_LIMIT_ERROR.formatted(channel, rlMax));
            }
            case 1 ->
                log.debug("[NotificationGuardService] Passed — tenantId: {} ,channel: {} ,usage: {}/{}",
                        tenantId, channel, count, rlMax);
            default ->
                throw new RateLimitException(
                        CHANNEL_RATE_LIMIT_ERROR);
        }
    }

    /**
     * Builds a Redis dedup key by hashing the canonical form of the notification request.
     * <p>
     * Canonical form: tenantId:channel:templateId:key1=val1|key2=val2 (variables sorted by key)
     * Hashed with SHA-256 to produce a fixed-length, collision-resistant key.
     */
    private String buildDedupKey(String tenantId,
                                  String channel,
                                  String templateId,
                                  Map<String, String> variables) {
        // Sort variable entries by key — ensures insertion-order independence
        // e.g. {name=Kevin, age=25} and {age=25, name=Kevin} produce the same hash
        String canonicalVars = (variables == null || variables.isEmpty())
                ? ""
                : variables.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("|"));

        String raw = tenantId + ":" + channel.toLowerCase() + ":" + templateId + ":" + canonicalVars;
        return DUPLICATE_KEY_PREFIX + CommonUtils.generateSHA256Hash(raw);
    }

    /**
     * Builds a Redis rate limit key scoped to the current UTC hour window.
     * <p>
     * Format: notif:rl:{tenantId}:{channel}:{hourEpoch}
     * The hourEpoch changes every hour, so the counter resets naturally without
     * needing an explicit DEL — old keys simply expire via their TTL.
     */
    private String buildRateLimitKey(String tenantId, String channel) {
        long hourWindow = Instant.now().getEpochSecond() / 3600;
        return RATE_LIMiT__KEY_PREFIX + tenantId + ":" + channel.toLowerCase() + ":" + hourWindow;
    }
}
