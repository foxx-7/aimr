package com.aimr.notify.infra.redis;

import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

@Configuration
public class RateLimitConfig {

    /**
     * Fixed window rate limiting + dedup in a single Redis round trip.
     * <p>
     * KEYS:
     *   KEYS[1] = dedup key      (notif:dedup:{tenantId}:{channel}:{hash})
     *   KEYS[2] = rate limit key (notif:rl:{tenantId}:{channel}:{hourWindow})
     * <p>
     * ARGV:
     *   ARGV[1] = rate limit max (int)
     *   ARGV[2] = dedup TTL in seconds
     *   ARGV[3] = rate limit window TTL in seconds
     * <p>
     * Returns List<Long> [status, count]:
     *   [ 1,  count] = OK
     *   [ 0,   -1  ] = DUPLICATE
     *   [-1,  count] = RATE LIMITED
     */
    @Bean
    @SuppressWarnings("unchecked")
    public RedisScript<@NonNull List<Long>> fixedWindowGuardScript() {
        String script = """
                local dup_key = KEYS[1]
                local rl_key  = KEYS[2]
                local rl_max  = tonumber(ARGV[1])
                local dup_ttl = tonumber(ARGV[2])
                local rl_ttl  = tonumber(ARGV[3])

                local set_result = redis.call('SET', dup_key, '1', 'NX', 'EX', dup_ttl)
                if not set_result then
                    return {0, -1}
                end

                local count = redis.call('INCR', rl_key)

                if count == 1 then
                    redis.call('EXPIRE', rl_key, rl_ttl)
                end

                if count > rl_max then
                    redis.call('DEL', dup_key)
                    return {-1, count}
                end

                return {1, count}
                """;

        return RedisScript.of(script, (Class<List<Long>>) (Class<?>) List.class);
    }

    /**
     * Token bucket rate limiting + dedup in a single Redis round trip.
     * Allows bursting up to bucket capacity, with continuous token refill.
     * <p>
     * KEYS:
     *   KEYS[1] = dedup key   (notif:dedup:{tenantId}:{channel}:{hash})
     *   KEYS[2] = bucket key  (notif:bucket:{tenantId}:{channel})
     * <p>
     * ARGV:
     *   ARGV[1] = bucket capacity (max burst size)
     *   ARGV[2] = refill rate (tokens per second, can be decimal e.g. 0.5)
     *   ARGV[3] = current time in milliseconds
     *   ARGV[4] = dedup TTL in seconds
     * <p>
     * Returns List<Long> [status, tokens_remaining]:
     *   [ 1,  tokens] = OK
     *   [ 0,     -1 ] = DUPLICATE
     *   [-1,      0 ] = RATE LIMITED (bucket empty)
     */
    @Bean
    @SuppressWarnings("unchecked")
    public RedisScript<@NonNull List<Long>> tokenBucketGuardScript() {
        String script = """
                local dup_key      = KEYS[1]
                local bucket_key   = KEYS[2]
                local capacity     = tonumber(ARGV[1])
                local refill_rate  = tonumber(ARGV[2])
                local now          = tonumber(ARGV[3])
                local dup_ttl      = tonumber(ARGV[4])

                -- Dedup check
                local set_result = redis.call('SET', dup_key, '1', 'NX', 'EX', dup_ttl)
                if not set_result then
                    return {0, -1}
                end

                -- Read current bucket state
                local bucket      = redis.call('HMGET', bucket_key, 'tokens', 'last_refill')
                local tokens      = tonumber(bucket[1])
                local last_refill = tonumber(bucket[2])

                -- First request: fill the bucket
                if not tokens then
                    tokens      = capacity
                    last_refill = now
                end

                -- Refill proportionally based on elapsed time
                local elapsed = math.max(0, now - last_refill)
                local refill  = math.floor(elapsed * refill_rate / 1000)
                if refill > 0 then
                    tokens      = math.min(capacity, tokens + refill)
                    last_refill = now
                end

                -- Bucket empty → rate limited, rollback dedup key
                if tokens <= 0 then
                    redis.call('DEL', dup_key)
                    return {-1, 0}
                end

                -- Consume one token and persist bucket state
                tokens = tokens - 1
                local bucket_ttl = math.ceil(capacity / refill_rate) + 60
                redis.call('HMSET', bucket_key, 'tokens', tokens, 'last_refill', last_refill)
                redis.call('EXPIRE', bucket_key, bucket_ttl)

                return {1, tokens}
                """;

        return RedisScript.of(script, (Class<List<Long>>) (Class<?>) List.class);
    }

    /**
     * Broadcast inflight slot tracking.
     * <p>
     * KEYS[1] = inflight counter key (notif:broadcast:inflight:{tenantId})
     * ARGV[1] = max concurrent broadcasts
     * ARGV[2] = TTL in seconds (crash-safety net)
     * <p>
     * Returns List<Long> [status, current]:
     *   [ 1, current] = slot acquired
     *   [-1, current] = max inflight reached
     */
    @Bean
    @SuppressWarnings("unchecked")
    public RedisScript<@NonNull List<Long>> broadcastInflightScript() {
        String script = """
                local key     = KEYS[1]
                local max     = tonumber(ARGV[1])
                local ttl     = tonumber(ARGV[2])

                local current = tonumber(redis.call('GET', key) or '0')

                if current >= max then
                    return {-1, current}
                end

                local newVal = redis.call('INCR', key)

                if newVal == 1 then
                    redis.call('EXPIRE', key, ttl)
                end

                return {1, newVal}
                """;
        return RedisScript.of(script, (Class<List<Long>>) (Class<?>) List.class);
    }
}
