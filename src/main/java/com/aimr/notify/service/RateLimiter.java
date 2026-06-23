package com.aimr.notify.service;

import com.aimr.notify.infra.redis.RateLimitProperties;
import com.aimr.notify.exception.RateLimitException;
import com.aimr.notify.exception.ValidationException;
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
public class RateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<@NonNull List<Long>> fixedWindowGuardScript;
    private final RedisScript<@NonNull List<Long>> tokenBucketGuardScript;
    private final RedisScript<@NonNull List<Long>> broadcastInflightScript;
    private final RateLimitProperties properties;

    /**
     * Performs a single atomic Redis check covering:
     *   1. Duplicate detection   — same tenantId + channel + templateId + variables within dupWindowSeconds
     *   2. Rate limit enforcement — fixed window, max requests per hour per tenant per channel
     * <p>
     * Must be called at the very start of sendNotification(), before any DB or Kafka work.
     *
     * @param tenantId   tenant making the request (from TenantContext)
     * @param channel    notification channel (e.g. "email", "sms", "push")
     * @param templateId template identifier used for this notification
     * @param variables  dynamic template variables map (order-insensitive)
     * @throws ValidationException if an identical request was made within the window
     * @throws RateLimitException  if the tenant has exceeded the channel rate limit
     */
    public void checkAndGuardWithFixedWindow(String tenantId,
                                             String channel,
                                             String templateId,
                                             Map<String, String> variables) {

        String dedupKey = buildDedupKey(tenantId, channel, templateId, variables);
        String rlKey    = buildRateLimitKey(tenantId, channel);
        int    rlMax    = properties.limitFor(channel);
        int    dupTtl   = properties.dupWindowSeconds();

        log.debug("[Guard:FixedWindow] Checking — tenantId={} channel={} templateId={} rlMax={} dupTtl={}s",
                tenantId, channel, templateId, rlMax, dupTtl);

        List<Long> result = redisTemplate.execute(
                fixedWindowGuardScript,
                List.of(dedupKey, rlKey),
                String.valueOf(rlMax),
                String.valueOf(dupTtl),
                String.valueOf(RATE_LIMIT_WINDOW_SECONDS)
        );

        handleGuardResult(result, tenantId, channel, templateId, rlMax);
    }

    /**
     * Performs a single atomic Redis check covering:
     *   1. Duplicate detection   — same tenantId + channel + templateId + variables within dupWindowSeconds
     *   2. Rate limit enforcement — token bucket, allows bursting up to bucketCapacity with continuous refill
     * <p>
     * Must be called at the very start of sendNotification(), before any DB or Kafka work.
     *
     * @param tenantId   tenant making the request (from TenantContext)
     * @param channel    notification channel (e.g. "email", "sms", "push")
     * @param templateId template identifier used for this notification
     * @param variables  dynamic template variables map (order-insensitive)
     * @throws ValidationException if an identical request was made within the window
     * @throws RateLimitException  if the tenant's token bucket is empty
     */
    public void checkAndGuardWithTokenBucket(String tenantId,
                                             String channel,
                                             String templateId,
                                             Map<String, String> variables) {

        String dedupKey   = buildDedupKey(tenantId, channel, templateId, variables);
        String bucketKey  = buildBucketKey(tenantId, channel);
        int    capacity   = properties.bucketCapacity();
        double refillRate = properties.refillRatePerSecond();
        int    dupTtl     = properties.dupWindowSeconds();
        long   nowMs      = Instant.now().toEpochMilli();

        log.debug("[Guard:TokenBucket] Checking — tenantId={} channel={} templateId={} capacity={} refillRate={}/s",
                tenantId, channel, templateId, capacity, refillRate);

        List<Long> result = redisTemplate.execute(
                tokenBucketGuardScript,
                List.of(dedupKey, bucketKey),
                String.valueOf(capacity),
                String.valueOf(refillRate),
                String.valueOf(nowMs),
                String.valueOf(dupTtl)
        );

        handleGuardResult(result, tenantId, channel, templateId, capacity);
    }

    /**
     * Shared result handler for both fixed window and token bucket guard scripts.
     * Both scripts return the same [status, count/tokens] contract:
     *   [ 1,  value] = OK
     *   [ 0,   -1  ] = DUPLICATE
     *   [-1,  value] = RATE LIMITED
     */
    private void handleGuardResult(List<Long> result,
                                   String tenantId,
                                   String channel,
                                   String templateId,
                                   int limit) {
        if (result == null || result.isEmpty()) {
            log.error("[Guard] Redis Lua script returned null — tenantId={} channel={}", tenantId, channel);
            throw new ValidationException(NOTIFICATION_GUARD_CHECK_ERROR);
        }

        long status = result.get(0);
        long value  = result.get(1);

        switch ((int) status) {
            case 0 -> {
                int windowMinutes = properties.dupWindowSeconds() / 60;
                log.warn("[Guard] Duplicate blocked — tenantId={} channel={} templateId={}",
                        tenantId, channel, templateId);
                throw new ValidationException(DUPLICATE_NOTIFICATION_REQUEST_ERROR.formatted(windowMinutes));
            }
            case -1 -> {
                log.warn("[Guard] Rate limit breached — tenantId={} channel={} value={} limit={}",
                        tenantId, channel, value, limit);
                throw new RateLimitException(CHANNEL_RATE_LIMIT_ERROR.formatted(channel, limit));
            }
            case 1 ->
                log.debug("[Guard] Passed — tenantId={} channel={} remaining={}/{}",
                        tenantId, channel, value, limit);
            default ->
                throw new RateLimitException(CHANNEL_RATE_LIMIT_ERROR);
        }
    }

    /**
     * Atomically checks and increments the in-flight broadcast counter for a tenant.
     * Rejects if the tenant already has the maximum number of concurrent broadcasts running.
     * <p>
     * The counter is auto-expired via TTL as a safety net in case the worker crashes
     * and never calls releaseInflightSlot() to decrement.
     *
     * @param tenantId tenant making the request (from TenantContext)
     * @throws RateLimitException if the tenant has reached the concurrent broadcast limit
     */
    public void checkBroadcastInflight(String tenantId) {

        String inflightKey = buildInflightKey(tenantId);
        int    maxInflight = properties.maxConcurrentBroadcasts();
        int    ttlSeconds  = properties.broadcastInflightTtlSeconds();

        log.debug("[BroadcastGuard] Checking inflight — tenantId={} max={}", tenantId, maxInflight);

        List<Long> result = redisTemplate.execute(
                broadcastInflightScript,
                List.of(inflightKey),
                String.valueOf(maxInflight),
                String.valueOf(ttlSeconds)
        );

        if (result == null || result.isEmpty()) {
            log.error("[BroadcastGuard] Redis Lua script returned null — tenantId={}", tenantId);
            throw new ValidationException(NOTIFICATION_GUARD_CHECK_ERROR);
        }

        long status  = result.get(0);
        long current = result.get(1);

        switch ((int) status) {
            case -1 -> {
                log.warn("[BroadcastGuard] Inflight limit breached — tenantId={} current={} max={}",
                        tenantId, current, maxInflight);
                throw new RateLimitException(
                        ("Broadcast limit reached. You already have %d concurrent broadcast(s) in progress. " +
                                "Please wait for one to complete before starting another.").formatted(maxInflight));
            }
            case 1 ->
                log.debug("[BroadcastGuard] Inflight slot acquired — tenantId={} current={}/{}",
                        tenantId, current, maxInflight);
            default ->
                throw new RateLimitException(NOTIFICATION_GUARD_CHECK_ERROR);
        }
    }

    /**
     * Decrements the in-flight broadcast counter for a tenant.
     * Must be called by the worker (BroadcastFanOutConsumer) when a batch
     * reaches a terminal state — COMPLETED, PARTIALLY_FAILED, or FAILED.
     *
     * @param tenantId tenant whose slot is being released
     */
    public void releaseInflightSlot(String tenantId) {

        String inflightKey = buildInflightKey(tenantId);
        Long current = redisTemplate.opsForValue().decrement(inflightKey);

        if (current != null && current < 0) {
            redisTemplate.opsForValue().set(inflightKey, "0");
            log.warn("[BroadcastGuard] Inflight counter went negative, reset to 0 — tenantId={}", tenantId);
        }

        log.debug("[BroadcastGuard] Inflight slot released — tenantId={} remaining={}", tenantId, current);
    }

    /**
     * Builds a Redis dedup key by hashing the canonical form of the notification request.
     * Canonical form: tenantId:channel:templateId:key1=val1|key2=val2 (variables sorted by key)
     * Hashed with SHA-256 to produce a fixed-length, collision-resistant key.
     */
    private String buildDedupKey(String tenantId,
                                 String channel,
                                 String templateId,
                                 Map<String, String> variables) {
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
     * Format: notif:rl:{tenantId}:{channel}:{hourEpoch}
     * The hourEpoch changes every hour so the counter resets naturally without explicit DEL.
     */
    private String buildRateLimitKey(String tenantId, String channel) {
        long hourWindow = Instant.now().getEpochSecond() / 3600;
        return RATE_LIMiT__KEY_PREFIX + tenantId + ":" + channel.toLowerCase() + ":" + hourWindow;
    }

    /**
     * Builds a Redis key for the token bucket state of a tenant+channel pair.
     * Format: notif:bucket:{tenantId}:{channel}
     * Persists across requests — the bucket state (tokens + last_refill) lives here.
     */
    private String buildBucketKey(String tenantId, String channel) {
        return "notif:bucket:" + tenantId + ":" + channel.toLowerCase();
    }

    /**
     * Builds a Redis key scoped to the tenant's concurrent broadcast counter.
     * Format: broadcast:inflight:{tenantId}
     */
    private String buildInflightKey(String tenantId) {
        return "broadcast:inflight:" + tenantId;
    }
}
